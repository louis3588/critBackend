package com.lp.criticabackend.model.request;

import java.util.ArrayList;
import java.util.List;

public class ArtistSearchResult {

    private String id;
    private String name;
    private List<String> imageUrls;

    public ArtistSearchResult(String id, String name, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.imageUrls = imageUrls;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}