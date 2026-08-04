package com.lp.criticabackend.model;

public class ArtistSongStats {
    private String spotifyUrl;
    private String title;
    private Long totalStreams;
    private Long dailyStreams;

    public ArtistSongStats(String spotifyUrl, String title, Long totalStreams, Long dailyStreams) {
        this.spotifyUrl = spotifyUrl;
        this.title = title;
        this.totalStreams = totalStreams;
        this.dailyStreams = dailyStreams;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTotalStreams() {
        return totalStreams;
    }

    public void setTotalStreams(Long totalStreams) {
        this.totalStreams = totalStreams;
    }

    public Long getDailyStreams() {
        return dailyStreams;
    }

    public void setDailyStreams(Long dailyStreams) {
        this.dailyStreams = dailyStreams;
    }
}
