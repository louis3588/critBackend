package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Integer> {

    List<Song> findByTitleContainingIgnoreCase(String title);
    Optional<Song> findSongBySpotifyUrl(String spotifyUrl);
}
