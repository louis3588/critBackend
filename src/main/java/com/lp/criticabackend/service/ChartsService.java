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
import reactor.netty.http.client.HttpClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChartsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AppLogger log = AppLogger.getLogger(ChartsService.class);
    private static final HttpClient httpClient = WebUtil.httpClient();
    private final SpotifyAuth spotifyAuth;
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

        Elements rows = table.select("tbody tr");

        for (Element row : rows) {
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

                Song song;
                if(token != null){
                    song = fetchMetaData(new Song(title, artist), trackId, token);
                } else {
                    song = new Song(title, artist);
                }

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
                log.warn("Skipping malformed row " + e.getMessage());
            }
        }
        return chartItems;
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

    private Song fetchMetaData(Song song, String trackId,String token){
        song.setSpotifyUrl(trackId);
        try {
            String trackUri = "https://api.spotify.com/v1/tracks/" + trackId;

            String res = webClient
                    .get()
                    .uri(trackUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (res == null || res.isEmpty()) {
                log.error("Empty Spotify response for track: {}" + trackId);
                return song;
            }

            JsonNode track = objectMapper.readTree(res);
            JsonNode album = track.path("album");

            String spotifyUrl = track.path("external_urls").path("spotify").asText(null);
            if (spotifyUrl != null) song.setSpotifyUrl(spotifyUrl);

            String albumName = album.path("name").asText(null);
            if (albumName != null) song.setAlbum(albumName);

            JsonNode images = album.path("images");
            if (!images.isEmpty()) {
                String coverUrl = images.get(0).path("url").asText(null);
                if (coverUrl != null) song.setCoverArtUrl(coverUrl);
            }

        } catch (Exception e) {
            log.error("Failed to fetch metadata for track "+  trackId, e);
        }
        return song;
    }
}
