package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.ChartSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, Integer> {

    boolean existsByCountryAndDate(String country, LocalDate date);

    Optional<ChartSnapshot> findByCountryAndDate(String country, LocalDate date);
    List<ChartSnapshot> findByCountryOrderByDateDesc(String country);
    Optional<ChartSnapshot> findTopByCountryOrderByDateDesc(String country);
    List<ChartSnapshot> findByCountryAndDateBetweenOrderByDateDesc(
            String country, LocalDate from, LocalDate to);
}
