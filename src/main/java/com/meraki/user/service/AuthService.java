package com.meraki.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meraki.constants.ApiConstants;
import com.meraki.user.dto.AccessToken;
import com.meraki.user.dto.TokenResponse;
import com.meraki.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class AuthService {

    @Autowired
    JwtUtils jwtUtils;

    public TokenResponse createAuthTokens(String userId, String phoneNumber) {

        try {

            String accessTokenStr = jwtUtils.generateToken(userId, ApiConstants.ACCESS_TOKEN_VALIDITY_SECONDS, phoneNumber);
            Instant now = Instant.now();
            Instant accessExpiry = now.plusSeconds(ApiConstants.ACCESS_TOKEN_VALIDITY_SECONDS);
            Timestamp accessExpiryTimestamp = Timestamp.from(accessExpiry);
            return new TokenResponse(accessTokenStr, userId, accessExpiryTimestamp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String validateTokenAndGetUserId(String token) {

        try {

            Claims claims = jwtUtils.validateToken(token);

            String userId = claims.getSubject();
            Date expiry = claims.getExpiration();

            Instant now = Instant.now();
            if (now.isBefore(Instant.ofEpochSecond(expiry.getTime())))
                return userId;

        } catch (Exception e) {
            log.error("Error while validating accessToken", e);
            return null;
        }

        return null;
    }

}
