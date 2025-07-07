package com.viberato.aldio.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserLoginRequest {

    private String username;
    private String password;

    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
