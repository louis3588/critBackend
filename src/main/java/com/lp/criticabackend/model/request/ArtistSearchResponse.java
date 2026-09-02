package com.lp.criticabackend.model.request;

import java.util.List;

public class ArtistSearchResponse extends SearchReponse{
    private List<ArtistSearchResult> artists;


    public ArtistSearchResponse(List<ArtistSearchResult> artists, int total, int offset, int limit) {
        super(total, offset, limit);
        this.artists = artists;
    }

    public List<ArtistSearchResult> getArtists() { return artists; }
}
