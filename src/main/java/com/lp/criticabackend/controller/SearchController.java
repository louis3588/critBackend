package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.service.SongSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SongSearchService songSearchService;

    public SearchController(SongSearchService songSearchService) {
        this.songSearchService = songSearchService;
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String query, @RequestParam String limit) {
        List<Song> results = songSearchService.search(query, limit);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(results);
        }
    }
}
