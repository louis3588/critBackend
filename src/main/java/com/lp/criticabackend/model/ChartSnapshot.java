package com.lp.criticabackend.model;

import java.time.LocalDate;
import java.util.List;

public class ChartSnapshot {

    private Integer id;

    private LocalDate date;

    private String country;

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
        this.chart = chart;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
