package com.lp.criticabackend.model.request;

public class SearchResponse {

    private int total;
    private int offset;
    private int limit;
    private boolean hasNext;

    public SearchResponse(int total, int offset, int limit) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.hasNext = offset + limit < total;
    }

}
