package com.lp.criticabackend.model.request;

import com.lp.criticabackend.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongSearchResponse extends SearchResponse{

    private List<Song> songs;

    public SongSearchResponse(List<Song> songs, int total, int offset, int limit) {
        super(total, offset, limit);
        this.songs = songs;
    }

    public List<Song> getSongs() {
        return songs;
    }
}
