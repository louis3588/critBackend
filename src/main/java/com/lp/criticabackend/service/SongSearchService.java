package com.lp.criticabackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Album;
import com.lp.criticabackend.model.Artist;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.model.request.ArtistSearchResponse;
import com.lp.criticabackend.model.request.ArtistSearchResult;
import com.lp.criticabackend.repos.SongRepository;
import com.lp.criticabackend.security.SpotifyAuth;
import com.lp.criticabackend.security.util.WebUtil;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class SongSearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpotifyAuth spotifyAuth;
    private final SongRepository songRepository;
    private static final AppLogger log = AppLogger.getLogger(SongSearchService.class);
    private static final HttpClient httpClient = WebUtil.httpClient();
    private final ChartsService chartsService;


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

    public List<Song> parseAlbumSafe(String albumId){
        String token = spotifyAuth.getToken();
        if (token == null || token.isEmpty()) {
            log.error("No token available for search");
            return new ArrayList<>();
        }

        List<Song> albumSongs = getAlbumSongs(albumId, token);
        List<Song> enrichedSongsList = new ArrayList<>();
        for(Song song : albumSongs){
            String songId = song
                    .getSpotifyUrl()
                    .split("/track/")[1];
            Song enrichedSong = chartsService.fetchMetaData(song, songId, token);
            enrichedSongsList.add(enrichedSong);
        }

        return enrichedSongsList;
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


                    String spotifyUrl = trackNode.path("external_urls").path("spotify").asText(null);

                    String artistId = artistArray.get(0).path("id").asText(null);
                    JsonNode album = trackNode.path("album");
                    String albumName = album.path("name").asText("Unknown Album");

                    JsonNode images = album.path("images");

                    String imageString = "";
                    if(!images.isEmpty()){
                        imageString = images.get(0).path("url").asText(null);
                    }

                    String releaseDate = album.path("release_date").asText(null);
                    Integer popularity = trackNode.path("popularity").asInt(0);

                    Song song = new Song(title, spotifyUrl, artist, albumName, imageString);
                    song.setReleaseDate(releaseDate);
                    song.setPopularity(popularity);
                    song.setAlbumId(albumId);
                    song.setArtistId(artistId);

                    results.add(song);
                }

                offset += limit;
                hasMore = offset < total;
            }
        } catch (Exception e) {
            log.error("Failed to album tracks for query: " + albumId, e);
            return results;
        }

        return results;

    }

    public ArtistSearchResponse searchArtistByQuery(String query, int offset, int limit){
        String token = spotifyAuth.getToken();
        List<ArtistSearchResult> results = new ArrayList<>();

        if (token == null || token.isEmpty()) {
            log.error("No token available for search: " + query);
            return new ArtistSearchResponse(results, 0, offset, limit);
        }

        try{

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUri = "https://api.spotify.com/v1/search?q=" + encodedQuery
                    + "&type=artist&limit=" + limit+ "&offset=" + offset;

            String res = webClient
                    .get()
                    .uri(searchUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if(res == null || res.isEmpty()) {
                log.warn("Empty response for query: " + query);
                return new ArtistSearchResponse(results, 0, offset, limit);
            }

            JsonNode json = objectMapper.readTree(res);
            JsonNode artists = json.path("artists");
            JsonNode items = artists.path("items");
            int total = artists.path("total").asInt(0);

            for(JsonNode artistNode : items){
                String artistId = artistNode.path("id").asText(null);
                if (artistId == null) continue;

                String name = artistNode.path("name").asText(null);
                List<String> imageUrls = new ArrayList<>();
                for(JsonNode imageNode : artistNode.path("images")){
                    String imageUrl = imageNode.path("url").asText(null);
                    if(imageUrl != null){
                        imageUrls.add(imageUrl);
                    }
                }

                results.add(new ArtistSearchResult(artistId, name, imageUrls));
            }

            return new ArtistSearchResponse(results, total, offset, limit);

        } catch (Exception e) {
            log.error("Failed to search for query: " + query, e);
            return new ArtistSearchResponse(results, 0, offset, limit);
        }
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

    private List<Album> getArtistDiscography(String artistId, String token) {
        List<Album> discography = new ArrayList<>();

        try {
            int offset = 0;
            int limit = 50;
            boolean hasMore = true;

            while (hasMore) {
                String res = webClient
                        .get()
                        .uri("https://api.spotify.com/v1/artists/" + artistId
                                + "/albums?limit=" + limit + "&offset=" + offset
                                + "&include_groups=album,single")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (res == null || res.isEmpty()) {
                    log.warn("Empty discography response for artist: " + artistId);
                    break;
                }

                JsonNode json = objectMapper.readTree(res);
                JsonNode items = json.path("items");
                int total = json.path("total").asInt(0);

                for (JsonNode albumNode : items) {
                    String albumId = albumNode.path("id").asText(null);
                    if (albumId == null) continue;

                    String title       = albumNode.path("name").asText("Unknown Album");
                    String releaseDate = albumNode.path("release_date").asText(null);

                    JsonNode images = albumNode.path("images");
                    String imageUrl = images.isEmpty()
                            ? null
                            : images.get(0).path("url").asText(null);

                    JsonNode artistNames = albumNode.path("artists");
                    String artistName = artistNames.isEmpty()
                            ? "Unknown Artist"
                            : artistNames.get(0).path("name").asText("Unknown Artist");

                    Album album = new Album();
                    album.setId(albumId);
                    album.setTitle(title);
                    album.setArtist(artistName);
                    album.setImageUrl(imageUrl);
                    album.setReleaseDate(releaseDate);
                    album.setSongs(new ArrayList<>());
                    album.setAverageRating(0.0);

                    discography.add(album);
                }

                offset += limit;
                hasMore = offset < total;
            }

        } catch (Exception e) {
            log.error("Failed to fetch discography for artist: " + artistId, e);
        }

        return discography;
    }
}
