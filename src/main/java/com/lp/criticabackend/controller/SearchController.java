package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Album;
import com.lp.criticabackend.model.Artist;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.service.SongSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SongSearchService songSearchService;

    public SearchController(SongSearchService songSearchService) {
        this.songSearchService = songSearchService;
    }

    @GetMapping("/song")
    public ResponseEntity<?> search(@RequestParam String query, @RequestParam String limit) {
        List<Song> results = songSearchService.search(query, limit);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(results);
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
}
