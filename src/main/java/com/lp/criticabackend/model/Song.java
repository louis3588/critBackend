package com.lp.criticabackend.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "song")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;

    @Column(name = "spotify_url", unique = true)
    private String spotifyUrl;

    @Column(name = "cover_art_url")
    private String coverArtUrl;

    // --- Constructors ---

    public Song() {}

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

    // --- Getters & Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getSpotifyUrl() { return spotifyUrl; }
    public void setSpotifyUrl(String spotifyUrl) { this.spotifyUrl = spotifyUrl; }

    public String getCoverArtUrl() { return coverArtUrl; }
    public void setCoverArtUrl(String coverArtUrl) { this.coverArtUrl = coverArtUrl; }
}
