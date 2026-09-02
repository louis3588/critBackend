package com.lp.criticabackend.model.request;

public class SearchReponse {

    private int total;
    private int offset;
    private int limit;
    private boolean hasNext;

    public SearchReponse(int total, int offset, int limit) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.hasNext = offset + limit < total;
    }

    public int getTotal() {
        return total;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
