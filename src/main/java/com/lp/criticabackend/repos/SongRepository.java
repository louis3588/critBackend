package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Integer> {

    List<Song> findByTitleContainingIgnoreCase(String title);
}
