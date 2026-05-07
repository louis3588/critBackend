package com.lp.criticabackend.service;

import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.ChartSnapshot;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.repos.ChartSnapshotRepository;
import com.lp.criticabackend.repos.SongRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChartPersistenceService {

    private final ChartSnapshotRepository snapshotRepository;
    private final SongRepository songRepository;
    private final ChartsService chartsService;
    private static final AppLogger log = AppLogger.getLogger(ChartPersistenceService.class);

    public ChartPersistenceService(ChartSnapshotRepository snapshotRepository,
                                   SongRepository songRepository,
                                   ChartsService chartsService) {
        this.snapshotRepository = snapshotRepository;
        this.songRepository = songRepository;
        this.chartsService = chartsService;
    }

    /**
     * Called on startup and then once every 24 hours.
     * Fetches and persists a snapshot for each country if one
     * does not already exist for today.
     */
    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *") // midnight every day
    public void syncDailySnapshots() {
        List<String> countries = List.of("global");
        countries.forEach(this::syncCountry);
    }

    private void syncCountry(String country) {
        LocalDate today = LocalDate.now();

        Optional<ChartSnapshot> latest = snapshotRepository
                .findTopByCountryOrderByDateDesc(country);

        if (latest.isPresent() && !latest.get().getDate().isBefore(today)) {
            log.info("Snapshot for {} is already up to date ");
            return;
        }

        log.info("No snapshot for {} today ({}), fetching.. " + country);
        fetchAndSave(country);
    }

    private void fetchAndSave(String country) {
        try {
            ChartSnapshot snapshot = chartsService.fetchChartSnapshot(country);

            snapshot.getChart()
                    .forEach(item -> item.setSong(resolveSong(item.getSong())));
            snapshotRepository.save(snapshot);
            log.info("Saved snapshot for " + country + snapshot.getDate());

        } catch (Exception e) {
            log.error("Failed to fetch and save snapshot for ", e);
        }
    }

    private Song resolveSong(Song song) {
        if (song.getSpotifyUrl() != null) {
            return songRepository.findSongBySpotifyUrl(song.getSpotifyUrl())
                    .orElseGet(() -> songRepository.save(song));
        }
        return songRepository.save(song);
    }
}
