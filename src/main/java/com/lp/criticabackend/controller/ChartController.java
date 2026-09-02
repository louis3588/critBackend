package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.ArtistSongStats;
import com.lp.criticabackend.model.ChartItem;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.service.ChartPersistenceService;
import com.lp.criticabackend.service.ChartsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final ChartPersistenceService chartPersistenceService;
    private final ChartsService chartsService;

    @Autowired
    public ChartController(ChartPersistenceService chartPersistenceService, ChartsService chartsService) {
        this.chartPersistenceService = chartPersistenceService;
        this.chartsService = chartsService;
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestChart(){
        LocalDate today = LocalDate.now();
        List<Song> chart = chartPersistenceService.getChartedSongs(today);
        if(chart.isEmpty()){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(chart);
        }
    }

    @GetMapping("/stats/artist/{artistId}")
    public ResponseEntity<?> getArtistSongStats(@PathVariable String artistId) {
        List<ArtistSongStats> stats = chartsService.getArtistSongStats(artistId);
        if (stats == null || stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok(stats);
        }
    }

    @GetMapping("/stats/song")
    public ResponseEntity<?> getSongStats(@RequestParam String artistId, String spotifyUrl) {
        Optional<ArtistSongStats> stat = chartsService.getSongStats(artistId, spotifyUrl);
        if(stat.isPresent()){
            return ResponseEntity.ok(stat.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
