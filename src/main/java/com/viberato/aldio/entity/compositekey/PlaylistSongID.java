package com.viberato.aldio.entity.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class PlaylistSongID implements Serializable {

    @Column(name = "playlist_id")
    private long playlistID;

    @Column(name = "song_id")
    private long songID;
}
