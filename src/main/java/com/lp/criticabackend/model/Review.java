package com.lp.criticabackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idreviews;

    public Integer getIdreviews() {
        return idreviews;
    }

    public void setIdreviews(Integer idreviews) {
        this.idreviews = idreviews;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getFirstListen() {
        return firstListen;
    }

    public void setFirstListen(Integer firstListen) {
        this.firstListen = firstListen;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "idusers")
    private User user;

    @Column(nullable = false, length = 120)
    private String songId;

    @Column(length = 120)
    private String title;

    @Column(length = 280)
    private String payload;

    private Integer rating = 0;
    private Integer firstListen = 0;

    @Column(length = 120)
    private String createdAt;

}

