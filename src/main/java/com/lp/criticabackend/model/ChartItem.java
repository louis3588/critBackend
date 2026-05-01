package com.lp.criticabackend.model;

public class ChartItem {

    private int id;
    private Integer position;
    private Integer positionGain;
    private Song song;
    private Integer daysCharting;
    private Integer dayStreams;
    private Integer dayStreamsGain;
    private Integer weekStreams;
    private Integer weekStreamsGain;
    private Integer totalStreams;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getPositionGain() {
        return positionGain;
    }

    public void setPositionGain(Integer positionGain) {
        this.positionGain = positionGain;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Integer getDaysCharting() {
        return daysCharting;
    }

    public void setDaysCharting(Integer daysCharting) {
        this.daysCharting = daysCharting;
    }

    public Integer getDayStreams() {
        return dayStreams;
    }

    public void setDayStreams(Integer dayStreams) {
        this.dayStreams = dayStreams;
    }

    public Integer getDayStreamsGain() {
        return dayStreamsGain;
    }

    public void setDayStreamsGain(Integer dayStreamsGain) {
        this.dayStreamsGain = dayStreamsGain;
    }

    public Integer getWeekStreams() {
        return weekStreams;
    }

    public void setWeekStreams(Integer weekStreams) {
        this.weekStreams = weekStreams;
    }

    public Integer getWeekStreamsGain() {
        return weekStreamsGain;
    }

    public void setWeekStreamsGain(Integer weekStreamsGain) {
        this.weekStreamsGain = weekStreamsGain;
    }

    public Integer getTotalStreams() {
        return totalStreams;
    }

    public void setTotalStreams(Integer totalStreams) {
        this.totalStreams = totalStreams;
    }

    public ChartItem(Integer position, Integer positionGain, Song song, Integer daysCharting, Integer dayStreams, Integer dayStreamsGain, Integer weekStreamsGain, Integer weekStreams, Integer totalStreams) {
        this.position = position;
        this.positionGain = positionGain;
        this.song = song;
        this.daysCharting = daysCharting;
        this.dayStreams = dayStreams;
        this.dayStreamsGain = dayStreamsGain;
        this.weekStreamsGain = weekStreamsGain;
        this.weekStreams = weekStreams;
        this.totalStreams = totalStreams;
    }
}
