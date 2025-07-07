package com.viberato.aldio.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class UserLoginResponse {

    private String username;
    private String firstName;
    private String secondName;
    private LocalDateTime createdAt;

    public UserLoginResponse(String username, String firstName, String secondName, LocalDateTime createdAt) {
        this.username = username;
        this.firstName = firstName;
        this.secondName = secondName;
        this.createdAt = createdAt;
    }
}
