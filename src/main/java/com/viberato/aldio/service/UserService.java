package com.viberato.aldio.service;

import com.viberato.aldio.DTO.UserLoginResponse;
import com.viberato.aldio.DTO.UserRegistrationRequest;
import com.viberato.aldio.DTO.UserLoginRequest;
import com.viberato.aldio.entity.User;
import com.viberato.aldio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    private boolean UsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    private User fetchUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("Username " + username + " not found")
        );
    }

    public UserLoginResponse registerUser(UserRegistrationRequest urr){
        if(UsernameExists(urr.getUsername())){
            throw new IllegalArgumentException("username already in use.");
        }

        User user = convertRegistrationRequestDTOtoUser(urr);

        return convertUserToLoginResponseDTO(userRepository.save(user));
    }

    public UserLoginResponse login(UserLoginRequest ulr){

        User user = fetchUserByUsername(ulr.getUsername());

        if (!encoder.matches(ulr.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("credentials do not match");
        }

        return convertUserToLoginResponseDTO(user);
    }

    private User convertRegistrationRequestDTOtoUser(UserRegistrationRequest urr) {
        User user = new User();
        user.setUsername(urr.getUsername());
        user.setPasswordHash(encoder.encode(urr.getPassword()));
        user.setFirstName(urr.getFirstName());
        user.setSecondName(urr.getSecondName());

        return user;
    }

    private UserLoginResponse convertUserToLoginResponseDTO(User user) {
        return new UserLoginResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getSecondName(),
                user.getCreatedAt());
    }
}
