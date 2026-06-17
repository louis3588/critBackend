package com.lp.criticabackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chart_snapshot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"country", "date"}))
public class ChartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate date;

    private String country;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChartItem> chart;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<ChartItem> getChart() {
        return chart;
    }

    public void setChart(List<ChartItem> chart) {
        this.chart = chart;
    }

    public ChartSnapshot(LocalDate date, String country, List<ChartItem> chart) {
        this.date = date;
        this.country = country;
        this.chart = new ArrayList<>();
        chart.forEach(this::addItem);
    }

    public ChartSnapshot() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void addItem(ChartItem item) {
        item.setSnapshot(this);
        this.chart.add(item);
    }
}
