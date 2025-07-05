package com.viberato.aldio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table( name = "user_stats" )
@Getter @Setter
public class UserStats {  // User statistics per song

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long statID;

    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private Song song;

    @Column(name = "play_count")
    private long playCount;

    @Min(0)
    @Max(5)
    @Column(name = "user_rating")
    private float userRating;

    @Column
    private boolean favourite;

    @Column(name = "last_played")
    private LocalDateTime lastPlayed;

    @PrePersist
    protected void onCreate() {
        if (lastPlayed == null) {
            lastPlayed = LocalDateTime.now();
        }
    }

    public UserStats() {}

    public UserStats(boolean favourite, float userRating, long playCount, Song song, User user) {
        this.favourite = favourite;
        this.userRating = userRating;
        this.playCount = playCount;
        this.song = song;
        this.user = user;
        this.lastPlayed = LocalDateTime.now();
    }
}
