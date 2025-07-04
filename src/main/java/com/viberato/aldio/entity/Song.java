package com.viberato.aldio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table( name = "songs")
@Getter @Setter
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long songID;

    @Column(nullable = false)
    private String songName;

    @Column(nullable = false)
    private String artistName;

    @Column(name = "duration", nullable = false)
    private int durationSeconds;

    @Column(name = "path", nullable = false)
    private String filepath;

    @Column(name = "genre_tags")
    private String genreTags;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "album_art_url", length = 1024)
    private String albumArtUrl;

    @Column(name = "release_year")
    private int releaseYear;

    // TO-DO --> METHOD to extract duration directly from file

    public Song() {}

    public Song(String songName, String artistName, int durationSeconds, String filepath) {
        this.songName = songName;
        this.artistName = artistName;
        this.durationSeconds = durationSeconds;
        this.filepath = filepath;
    }

    public Song(String songName, String artistName, int durationSeconds, String filepath, String genreTags, String albumName, String albumArtUrl, int releaseYear) {
        this.songName = songName;
        this.artistName = artistName;
        this.durationSeconds = durationSeconds;
        this.filepath = filepath;
        this.genreTags = genreTags;
        this.albumName = albumName;
        this.albumArtUrl = albumArtUrl;
        this.releaseYear = releaseYear;
    }
}
