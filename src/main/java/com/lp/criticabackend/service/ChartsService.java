package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.ChartItem;
import com.lp.criticabackend.model.ChartSnapshot;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.security.SpotifyAuth;
import com.lp.criticabackend.util.WebUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class ChartsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AppLogger log = AppLogger.getLogger(ChartsService.class);
    private static final HttpClient httpClient = WebUtil.httpClient();
    private final SpotifyAuth spotifyAuth;
    private final ExecutorService spotifyExe = Executors.newFixedThreadPool(5);


    public ChartsService(SpotifyAuth spotifyAuth) {
        this.spotifyAuth = spotifyAuth;
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public ChartSnapshot fetchChartSnapshot(String country) {
        String html = fetchHtml(country);
        if (html.isEmpty()) {
            return null;
        }

        LocalDate date = LocalDate.now();
        List<ChartItem> items = parseChartItems(html);

        return new ChartSnapshot(date, country, items);
    }

    private String fetchHtml(String country){
        String url = "https://kworb.net/spotify/country/global_daily.html";
        String html = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if(html == null || html.isEmpty()){
            log.warn("Failed to fetch charts");
            return "";
        }
        return html;
    }

    public List<Song> fetchCharts(String country){
        String html = fetchHtml(country);
        return parseCharts(html);
    }

    private List<Song> parseCharts(String html){
        return parseChartItems(html)
                .stream()
                .map(ChartItem::getSong)
                .toList();
    }

    private Long parseGainSafe(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return Long.parseLong(raw.replace(",", "").replace("+", "").trim());
        } catch (NumberFormatException e) {
            log.error("Failed to format number: {}", e);
            return null;
        }
    }

    private Integer parsePositionSafe(String raw, Integer position) {
        if (raw == null || raw.isEmpty()) return null;
        if(raw.contains("=")){
            return 0;
        }
        if(raw.contains("RE") || raw.contains("NEW")){
            return 201 - position;
        }
        try {
            return Integer.parseInt(raw.replace("+", "").trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse position gain: {}", e);
            return null;
        }
    }

    private String extractTitle(Element titleCell){
        Element titleEl = titleCell.selectFirst("a[href*=track]");
        return titleEl != null ? titleEl.text() : "Unknown Title";
    }

    private String extractArtist(Element artistCell){
        Elements artistLinks = artistCell.select("a[href*=artist]");
        List<String> names = artistLinks.stream()
                .map(Element::text)
                .toList();

        return names.isEmpty() ? "Unknown Artist" : names.get(0);
    }

    private List<ChartItem> parseChartItems(String html){
        List<ChartItem> chartItems = new ArrayList<>();
        String token = spotifyAuth.getToken();

        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");

        if(table == null){
            log.error("Charts table not found");
            return chartItems;
        }

        int count = 0;

        Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            count++;
            log.debug("Processing track "+ count);
            try{
                Elements cells = row.select("td");

                int position = Integer.parseInt(cells.get(0).text().trim());
                Integer positionGain = parsePositionSafe(cells.get(1).text().trim(), position);
                Element titleCell = cells.get(2);
                String title = extractTitle(titleCell);
                String artist = extractArtist(titleCell);
                String trackId = extractTrackId(titleCell);

                Integer daysCharting = parsePositionSafe(cells.get(3).text(), position);
                Element peakCountCell = row.selectFirst("td.mini.text");
                int streamsOffset = peakCountCell != null ? 0 : -1;

                Long dayStreams      = parseGainSafe(cells.get(6 + streamsOffset).text());
                Long dayStreamsGain  = parseGainSafe(cells.get(7 + streamsOffset).text());
                Long weekStreams     = parseGainSafe(cells.get(8 + streamsOffset).text());
                Long weekStreamsGain = parseGainSafe(cells.get(9 + streamsOffset).text());
                Long totalStreams    = parseGainSafe(cells.get(10 + streamsOffset).text());

                Song song = new Song(title, artist);
                song.setSpotifyUrl(trackId);

                ChartItem item = new ChartItem(
                        position,
                        positionGain,
                        song,
                        daysCharting,
                        dayStreams,
                        dayStreamsGain,
                        weekStreamsGain,
                        weekStreams,
                        totalStreams
                );

                chartItems.add(item);

            } catch (Exception e) {
                log.warn("Skipping malformed row: " + e.getMessage());
            }
        }

        if(token != null && !token.isEmpty()){
            List<List<ChartItem>> batches = partitionBatches(chartItems);

            for(List<ChartItem> batch : batches){
                List<CompletableFuture<Void>> futures = batch
                        .stream()
                        .map(item -> CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                Song song = item.getSong();
                                if(!song.getSpotifyUrl().isEmpty()){
                                    Song enriched = fetchMetaData(song, song.getSpotifyUrl(), token);
                                    item.setSong(enriched);
                                }
                            }
                        }, spotifyExe))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        }

        return chartItems;
    }

    public <T> List<List<T>> partitionBatches(List<T> list) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += 5) {
            partitions.add(list.subList(i, Math.min(i + 5, list.size())));
        }
        return partitions;
    }

    private String extractTrackId(Element titleCell){
        Element titleEl = titleCell.selectFirst("a[href*=track]");
        if(titleEl == null){
            log.warn("Failed to extract trackId");
            return "";
        } String href = titleEl.attr("href");
        String filename = href.substring(href.lastIndexOf("/") + 1);
        return filename.replace(".html", "");
    }

    public Song fetchMetaData(Song song, String trackId, String token) {

        try {
            log.debug("Fetching " + trackId);
            String trackUri =
                    "https://api.spotify.com/v1/tracks/" + trackId;

            String res = webClient
                    .get()
                    .uri(trackUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (res == null || res.isEmpty()) {
                return song;
            }

            JsonNode track = objectMapper.readTree(res);

            if(song == null){
                String title = track.path("name").asText("Unknown Track");

                JsonNode artists = track.path("artists");
                String artist = artists
                        .isEmpty()
                        ? "Unknown Artist"
                        : artists.get(0).path("name").asText("Unknown Artist");

                song = new Song(title, artist);
            }

            JsonNode album = track.path("album");

            song.setSpotifyUrl(
                    track.path("external_urls")
                            .path("spotify")
                            .asText(null)
            );

            song.setAlbum(album.path("name").asText(null));
            String albumString = album.path("name").asText(null);

            if (albumString != null) {
                log.debug("Found album: " + albumString);
            }
            JsonNode images = album.path("images");

            if (!images.isEmpty()) {
                song.setCoverArtUrl(
                        images.get(0).path("url").asText(null)
                );
            }

            song.setReleaseDate(album.path("release_date").asText(null));
            song.setPopularity(track.path("popularity").asInt(0));
            song.setAlbumId(album.path("id").asText(null));

        } catch (WebClientResponseException.TooManyRequests e) {

            String retryAfter =
                    e.getHeaders().getFirst("Retry-After");

            long waitSeconds =
                    retryAfter != null
                            ? Long.parseLong(retryAfter)
                            : 10;

            log.warn("Rate limited. Waiting "+ waitSeconds);

            try {
                Thread.sleep(waitSeconds * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            return fetchMetaData(song, trackId, token);

        } catch (Exception e) {

            log.error("Failed to fetch metadata for + " + trackId, e);
        }

        return song;
    }
}
