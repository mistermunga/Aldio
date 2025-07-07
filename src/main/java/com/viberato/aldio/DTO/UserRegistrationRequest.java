package com.viberato.aldio.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserRegistrationRequest {

    private String username;
    private String password;
    private String firstName;
    private String secondName;

    public UserRegistrationRequest(String username, String password, String firstName, String secondName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.secondName = secondName;
    }

}
