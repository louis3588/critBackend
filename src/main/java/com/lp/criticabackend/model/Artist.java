package com.lp.criticabackend.model;

import java.util.List;

public class Artist {
    private String id;
    private String name;
    private List<String> imageUrls;
    private List<Album> discography;
    private Double popularity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<Album> getDiscography() {
        return discography;
    }

    public void setDiscography(List<Album> discography) {
        this.discography = discography;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}
