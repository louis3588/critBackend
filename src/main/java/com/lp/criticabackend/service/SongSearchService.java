package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Album;
import com.lp.criticabackend.model.Artist;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.repos.SongRepository;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SongSearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpotifyAuth spotifyAuth;
    private final SongRepository songRepository;
    private static final AppLogger log = AppLogger.getLogger(SongSearchService.class);
    private static final HttpClient httpClient = WebUtil.httpClient();
    private final ChartsService chartsService;
    private final ExecutorService spotifyExe = Executors.newFixedThreadPool(5);


    public SongSearchService(SpotifyAuth spotifyAuth, SongRepository songRepository, ChartsService chartsService) {
        this.spotifyAuth = spotifyAuth;
        this.songRepository = songRepository;
        this.chartsService = chartsService;
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private Song getByUrl(String url, String token) {
        Optional<Song> songOptional = songRepository.findSongBySpotifyUrl(url);
        if (songOptional.isPresent()) {
            return songOptional.get();
        } else {
            Song song = new Song();
            int index = url.lastIndexOf("/");
            String trackId = url.substring(index + 1);
            return chartsService.fetchMetaData(song, trackId, token);
        }
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

                    Song song = new Song(title, spotifyUrl, artist, album, coverArt);
                    results.add(song);
                }
            } catch (Exception e) {
                log.error("Failed to search for query: " + query, e);
            }
        }
        return results;
    }

    private List<Song> getAlbumSongs(String albumId, String token){
        List<Song> results = new ArrayList<>();

        if (token == null || token.isEmpty()) {
            log.error("No token available for search: " + albumId);
            return results;
        }

        try{
            int offset = 0;
            int limit = 50;
            boolean hasMore = true;

            while (hasMore) {
                String uri = "https://api.spotify.com/v1/albums/" + albumId
                        + "/tracks?limit=" + limit + "&offset=" + offset;

                String res = webClient
                        .get()
                        .uri(uri)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if(res == null || res.isEmpty()) {
                    log.warn("Empty response for query: " + albumId);
                    break;
                }

                JsonNode json = objectMapper.readTree(res);
                JsonNode items = json.path("items");
                int total = json.path("total").asInt(0);

                for (JsonNode trackNode : items) {
                    String trackId = trackNode.path("id").asText("");
                    if(trackId.isEmpty()) {
                        continue;
                    }

                    String title = trackNode.path("name").asText("Unknown Track");
                    JsonNode artistArray = trackNode.path("artists");
                    String artist = artistArray.isEmpty()
                            ? "Unknown Artist"
                            : artistArray.get(0).path("name").asText("Unknown Artist");

                    Song savedSong = new Song(title, artist);

                    savedSong.setSpotifyUrl(trackId);
                    results.add(savedSong);
                }

                offset += limit;
                hasMore = offset < total;
            }
        } catch (Exception e) {
            log.error("Failed to album tracks for query: " + albumId, e);
            return results;
        }

        List<List<Song>> batches = chartsService.partitionBatches(results);

        for (List<Song> batch : batches) {
            List<CompletableFuture<Void>> futures = batch
                    .stream()
                    .map(track -> CompletableFuture.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            String trackId = track.getSpotifyUrl();
                            chartsService.fetchMetaData(track, trackId, token);
                        }
                    }, spotifyExe))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            try{
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return results;

    }

    public Album getAlbumFromSong(String url) {
        String token = spotifyAuth.getToken();

        String fullUrl = "https://open.spotify.com/track/" + url;

        Song song = getByUrl(fullUrl, token);

        Album album = new Album();

        album.setId(song.getAlbumId());
        album.setTitle(song.getAlbum());
        album.setArtist(song.getArtist());
        album.setImageUrl(song.getCoverArtUrl());
        album.setReleaseDate(song.getReleaseDate());

        List<Song> albumSongs = getAlbumSongs(song.getAlbumId(), token);
        album.setSongs(albumSongs);

        int cumalativePopularity = 0;
        double averagePopularity;
        for(Song songs : albumSongs){
            cumalativePopularity += songs.getPopularity();
        }

        if(cumalativePopularity > 0 && !albumSongs.isEmpty()){
            averagePopularity = (double) cumalativePopularity / albumSongs.size();
        } else {
            averagePopularity = 0;
        }
        album.setAverageRating(averagePopularity);

        return album;
    }

    public Artist getArtist(String artistId) {
        String token = spotifyAuth.getToken();
        Artist artist = new Artist();

        if (token == null || token.isEmpty()) {
            log.error("No token available for artist fetch: " + artistId);
            return artist;
        }

        try{
            String res = webClient
                    .get()
                    .uri("https://api.spotify.com/v1/artists/" + artistId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if(res == null || res.isEmpty()) {
                log.warn("Empty response for artist: " + artistId);
                return artist;
            }

            JsonNode json = objectMapper.readTree(res);

            artist.setId(artistId);

            String name = json.path("name").asText("Unknown Artist");
            artist.setName(name);

            List<String> imageUrls = new ArrayList<>();
            for(JsonNode image : json.path("images")){
                imageUrls.add(image.path("url").asText(null));
            }
            artist.setImageUrls(imageUrls);

            List<Album> discography = getArtistDiscography(artistId, token);

            double cumulativePopularity = discography
                    .stream()
                    .mapToDouble(a -> a.getAverageRating() != null ? a.getAverageRating() : 0)
                    .sum();
            double averagePopularity = discography
                    .isEmpty()
                    ? 0
                    : cumulativePopularity / discography.size();

            artist.setDiscography(discography);
            artist.setPopularity(averagePopularity);

        } catch (Exception e) {
            log.error("Failed to fetch for artist: " + artistId, e);
        }
        return artist;
    }

    private List<Album> getArtistDiscography(String artistId, String token){
        List<Album> discography = new ArrayList<>();

        try{
            int offset = 0;
            int limit = 10;
            boolean hasMore = true;
            List<JsonNode> albumNodes = new ArrayList<>();

            while (hasMore) {
                String res = webClient
                        .get()
                        .uri("https://api.spotify.com/v1/artists/" + artistId
                                + "/albums?limit=" + limit + "&offset=" + offset)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if(res == null || res.isEmpty()) {
                    log.warn("Empty response for artist code : " + artistId);
                    break;
                }

                JsonNode json = objectMapper.readTree(res);
                JsonNode items = json.path("items");
                int total = json.path("total").asInt(0);

                for(JsonNode albumNode : items){
                    albumNodes.add(albumNode);
                }

                offset += limit;
                hasMore = offset < total;
            }

            List<List<JsonNode>> batches = chartsService.partitionBatches(albumNodes);
            ConcurrentLinkedQueue<Album> albumQueue = new ConcurrentLinkedQueue<>();

            for (List<JsonNode> batch : batches) {
                List<CompletableFuture<Void>> futures = batch
                        .stream()
                        .map(album -> CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    String albumId = album.path("id").asText(null);
                                    String title = album.path("name").asText("Unknown Album");
                                    String releaseDate = album.path("release_date").asText(null);

                                    JsonNode images = album.path("images");
                                    String imageUrl = images.isEmpty()
                                            ? null
                                            : images.get(0).path("url").asText(null);

                                    JsonNode artistNames = album.path("artists");
                                    String artistName = artistNames.isEmpty()
                                            ? "Unknown Artist"
                                            : artistNames.get(0).path("name").asText("Unknown Artist");

                                    if(albumId == null || albumId.isEmpty()) {
                                        return;
                                    }

                                    List<Song> albumSongs = getAlbumSongs(albumId, token);

                                    int cumalativePopularity = albumSongs
                                            .stream()
                                            .mapToInt(s -> s.getPopularity() != null ? s.getPopularity() : 0)
                                            .sum();

                                    double averagePopularity = albumSongs
                                            .isEmpty()
                                            ? 0
                                            : (double) cumalativePopularity / albumSongs.size();

                                    Album albumObj = new Album();
                                    albumObj.setId(albumId);
                                    albumObj.setTitle(title);
                                    albumObj.setArtist(artistName);
                                    albumObj.setImageUrl(imageUrl);
                                    albumObj.setReleaseDate(releaseDate);
                                    albumObj.setSongs(albumSongs);
                                    albumObj.setAverageRating(averagePopularity);

                                    albumQueue.add(albumObj);

                                } catch (Exception e){
                                    log.error("Failed to build album in discography: ", e);
                                }
                            }
                        }, spotifyExe))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            discography.addAll(albumQueue);

        } catch (Exception e) {
            log.error("Failed to fetch discography for artist: " + artistId, e);
        }

        return discography;
    }
}
