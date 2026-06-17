package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.security.SpotifyAuth;
import com.lp.criticabackend.util.WebUtil;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SongSearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpotifyAuth spotifyAuth;
    private static final AppLogger log = AppLogger.getLogger(SongSearchService.class);
    private static final HttpClient httpClient = WebUtil.httpClient(25);


    public SongSearchService(SpotifyAuth spotifyAuth) {
        this.spotifyAuth = spotifyAuth;
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public List<Song> search(String query, String limit) {
        List<Song> results = new ArrayList<>();
        String token = spotifyAuth.getToken();

        if (token == null || token.isEmpty()) {
            log.error("No token available for search");
            return results;
        } else {

            try{
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String searchUri = "https://api.spotify.com/v1/search?q=" + encodedQuery
                        + "&type=track&limit=" + limit;

                String res = webClient
                        .get()
                        .uri(searchUri)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if(res == null || res.isEmpty()) {
                    log.warn("Empty response for query: " + query);
                    return results;
                }

                JsonNode json = objectMapper.readTree(res);
                JsonNode items = json.path("tracks").path("items");

                for (JsonNode track : items) {
                    if(!track.path("type").asText().equalsIgnoreCase("track")) {
                        continue;
                    }

                    String title = track.path("name").asText("Unknown Track");
                    if(title.equalsIgnoreCase("Unknown Track")) {
                        continue;
                    }

                    JsonNode artists = track.path("artists");
                    String artist = artists
                            .isEmpty()
                            ? "Unknown Artist"
                            : artists.get(0).path("name").asText("Unknown Artist");

                    JsonNode albumObj = track.path("album");
                    if(albumObj.isMissingNode()){
                        continue;
                    }
                    String album = albumObj.path("name").asText("Unknown Album");
                    String spotifyUrl = track.path("external_urls").path("spotify").asText("");
                    String coverArt = albumObj.path("images").isEmpty()
                            ? ""
                            : albumObj.path("images").get(0).path("url").asText("");

                    Song song = new Song(title, artist, album, spotifyUrl, coverArt);
                    results.add(song);
                }
            } catch (Exception e) {
                log.error("Failed to search for query: " + query, e);
            }
        }
        return results;
    }
}
