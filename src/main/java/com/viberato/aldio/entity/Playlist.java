package com.viberato.aldio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table( name = "playlists" )
@Getter @Setter
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long playlistID;

    @ManyToOne
    @JoinColumn
    private User user;

    @Column
    private int duration;  // total length in seconds

    // TO-DO --> add method to automatically compute length

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Playlist() {}

    public Playlist(User user) {
        this.user = user;
    }
}
