package com.lp.criticabackend.model;

public class Song {

    private String title;

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    private String spotifyUrl;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Song(String title, String spotifyUrl, String artist, String album, String coverArtUrl) {
        this.title = title;
        this.spotifyUrl = spotifyUrl;
        this.artist = artist;
        this.album = album;
        this.coverArtUrl = coverArtUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCoverArtUrl() {
        return coverArtUrl;
    }

    public void setCoverArtUrl(String coverArtUrl) {
        this.coverArtUrl = coverArtUrl;
    }

    private String artist;
    private String album;
    private String coverArtUrl;
}
