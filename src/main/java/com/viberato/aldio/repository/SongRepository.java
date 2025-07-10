package com.viberato.aldio.repository;

import com.viberato.aldio.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    Optional<Song> findByFilepath(String filepath);

    List<Song> findByTitle(String title);
    List<Song> findByArtist(String artist);
}
