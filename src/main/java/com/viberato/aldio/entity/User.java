package com.viberato.aldio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table( name = "users")
@Getter @Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userID;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
    }

    public User() {
    }

    public User(String username, String passwordHash, String firstName, String secondName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.secondName = secondName;
    }
}
