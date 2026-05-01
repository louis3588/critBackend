package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Song;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChartsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AppLogger log = AppLogger.getLogger(ChartsService.class);
    private static final HttpClient httpClient = WebUtil.httpClient();

    public ChartsService() {
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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
        List<Song> songs = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");

        if(table == null){
            log.error("Charts table not found");
            return songs;
        } else {
            Elements rows = table.select("tr");

            for (Element row : rows) {
                Element posEl = row.selectFirst("td.np");
                Element textEl = row.selectFirst("td.text.mp");

                if (posEl == null || textEl == null) continue;

                int position = Integer.parseInt(posEl.text());

                Element titleEl = textEl.selectFirst("a[href*=track]");
                String title = titleEl != null ? titleEl.text() : "Unknown Title";

                Elements artistLinks = textEl.select("a");
                List<String> artistNames = artistLinks.stream()
                        .map(Element::text)
                        .toList();

                String artists = String.join(", ", artistNames);

                Song song = new Song(position, title,
                        artists.isEmpty() ? "Unknown Artist" : artists);

                songs.add(new Song(position, title,
                        artists.isEmpty() ? "Unknown Artist" : artists));
            }
        }

        return songs;
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

                // Album name
                String albumName = album.path("name").asText(null);
                if (albumName != null) song.setAlbum(albumName);

                // Cover art (first image = highest resolution)
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
