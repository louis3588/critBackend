package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.ChartItem;
import com.lp.criticabackend.model.ChartSnapshot;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.security.SpotifyAuth;
import com.lp.criticabackend.util.WebUtil;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        String url = "https://kworb.net/spotify/country/" + country + "_weekly.html";
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

        String url = "https://kworb.net/spotify/country/" + country + "_weekly.html";
        String html = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if(html == null || html.isEmpty()){
            log.warn("Failed to fetch charts");
        }

        return parseCharts(html);
    }

    private List<Song> parseCharts(String html){
        return parseChartItems(html)
                .stream()
                .map(ChartItem::getSong)
                .toList();
    }




    private Integer parseGainSafe(String raw){
        if(raw == null || raw.isEmpty()) return null;
        try{
            return Integer.parseInt(raw.replace(",", "").replace("+", "").trim());
        } catch(NumberFormatException e){
            log.error("Failed to format neg or pos number", e);
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
                if (cells.size() < 11) continue;

                int position = Integer.parseInt(cells.get(0).text().trim());
                Integer positionGain = parseGainSafe(cells.get(1).text().trim());
                Element titleCell = cells.get(2);
                String title = extractTitle(titleCell);
                String artist = extractArtist(titleCell);
                Integer daysCharting = parseGainSafe(cells.get(3).text());
                Integer dayStreams      = parseGainSafe(cells.get(6).text());
                Integer dayStreamsGain  = parseGainSafe(cells.get(7).text());
                Integer weekStreams     = parseGainSafe(cells.get(8).text());
                Integer weekStreamsGain = parseGainSafe(cells.get(9).text());
                Integer totalStreams    = parseGainSafe(cells.get(10).text());

                Song song;
                if(token != null){
                    song = fetchMetaData(new Song(title, artist), token);
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

    private Song fetchMetaData(Song song, String token){
        try{
            String query = "track:" + song.getTitle() + " artist:" + song.getArtist();
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUri = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=1";

            String res = webClient
                    .get()
                    .uri(searchUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (res == null || res.isEmpty()) {
                log.error("Empty Spotify response");
                return song;
            }else {
                JsonNode json = objectMapper.readTree(res);
                JsonNode tracks = json.path("tracks");
                JsonNode items = tracks.path("items");

                if(items.isEmpty()){
                    log.warn("Failed to find items for song:" + song.getTitle());
                    return song;
                }

                JsonNode track = items.get(0);
                JsonNode album = track.path("album");

                // Spotify URL
                String spotifyUrl = track.path("external_urls").path("spotify").asText(null);
                if (spotifyUrl != null) song.setSpotifyUrl(spotifyUrl);

                String albumName = album.path("name").asText(null);
                if (albumName != null) song.setAlbum(albumName);

                JsonNode images = album.path("images");
                if (!images.isEmpty()) {
                    String coverUrl = images.get(0).path("url").asText(null);
                    if (coverUrl != null) song.setCoverArtUrl(coverUrl);
                }

            }
        }catch(Exception e){
            log.error("Failed to fetch meta data", e);
            return song;
        }

        return song;
    }
}
