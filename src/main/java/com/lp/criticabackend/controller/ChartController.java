package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.ChartItem;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.service.ChartPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final ChartPersistenceService chartPersistenceService;

    @Autowired
    public ChartController(ChartPersistenceService chartPersistenceService) {
        this.chartPersistenceService = chartPersistenceService;
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
}
