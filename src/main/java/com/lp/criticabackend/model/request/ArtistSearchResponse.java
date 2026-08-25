package com.lp.criticabackend.model.request;

import java.util.List;

public class ArtistSearchResponse {
    private List<ArtistSearchResult> artists;
    private int total;
    private int offset;
    private int limit;
    private boolean hasNext;

    public ArtistSearchResponse(List<ArtistSearchResult> artists, int total, int offset, int limit) {
        this.artists = artists;
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.hasNext = offset + limit < total;
    }

    public List<ArtistSearchResult> getArtists() { return artists; }
    public int getTotal() { return total; }
    public int getOffset() { return offset; }
    public int getLimit() { return limit; }
    public boolean isHasNext() { return hasNext; }
}
