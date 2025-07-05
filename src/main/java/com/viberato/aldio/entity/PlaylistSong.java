package com.viberato.aldio.entity;

import com.viberato.aldio.entity.compositekey.PlaylistSongID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table( name = "playlist_songs")
@Getter @Setter
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongID playlistSongID;

    @ManyToOne
    @MapsId("playlistID")
    @JoinColumn(nullable = false)
    private Playlist playlist;

    @ManyToOne
    @MapsId("songID")
    @JoinColumn(nullable = false)
    private Song song;
}
