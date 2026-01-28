package com.meraki.user.service;

import com.meraki.user.dto.AccessToken;
import com.meraki.user.dto.TokenResponse;
import com.meraki.user.dto.Users;
import com.meraki.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthService authService;

    public TokenResponse registerUser(String phoneNumber) {

        Optional<Users> usersOptional = userRepository.getUserByPhoneNumber(phoneNumber);
        if(usersOptional.isPresent()) {
            // If user is already present, create a new token and return back
            return authService.createAuthTokens(usersOptional.get().getUserId(), usersOptional.get().getPhoneNumber());
        }

        String userId = UUID.randomUUID().toString();
        Users user = new Users(
                userId,
                phoneNumber,
                Instant.now().getEpochSecond()
        );

        userRepository.saveUser(user);
        return authService.createAuthTokens(user.getUserId(), user.getPhoneNumber());
    }

    public boolean validateUserToken(String token) {

        String userId = authService.validateTokenAndGetUserId(token);
        return userRepository.userExists(userId);

    }
}
