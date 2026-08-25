package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Album;
import com.lp.criticabackend.model.Artist;
import com.lp.criticabackend.model.ArtistSongStats;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.model.request.ArtistSearchResponse;
import com.lp.criticabackend.service.ChartsService;
import com.lp.criticabackend.service.SongSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private final ChartsService chartsService;
    private final SongSearchService songSearchService;

    public SearchController(ChartsService chartsService, SongSearchService songSearchService) {
        this.chartsService = chartsService;
        this.songSearchService = songSearchService;
    }

    @GetMapping("/song")
    public ResponseEntity<?> searchSong(@RequestParam String query, @RequestParam String limit) {
        List<Song> results = songSearchService.search(query, limit);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(results);
        }
    }

    //for homepage search
    @GetMapping("/artistsquery")
    public ResponseEntity<?> searchArtistByQuery(@RequestParam String query, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        if(query == null || query.trim().length() < 3) {
            return ResponseEntity.noContent().build();
        }

        ArtistSearchResponse result = songSearchService.searchArtistByQuery(query, offset, limit);
        if(result == null) {
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(result);
        }

    }

    @GetMapping("/album/song/{songUrl}")
    public ResponseEntity<?> searchAlbumBySong(@PathVariable String songUrl) {
        Album album = songSearchService.getAlbumFromSong(songUrl);
        if (album == null || album.getSongs().isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(album);
        }
    }

    @GetMapping("/album/id/{albumId}")
    public ResponseEntity<?> searchAlbumById(@PathVariable String albumId){
        List<Song> album = songSearchService.parseAlbumSafe(albumId);
        if (album == null || album.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(album);
        }
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getArtist(@PathVariable String artistId) {
        Artist artist = songSearchService.getArtist(artistId);
        if (artist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(artist);
    }

    @GetMapping("/artist/{artistId}/stats")
    public ResponseEntity<?> getArtistSongStats(@PathVariable String artistId) {
        List<ArtistSongStats> stats = chartsService.getArtistSongStats(artistId);
        if (stats == null || stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok(stats);
        }
    }
}
