package com.meraki.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meraki.constants.ApiConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    private Key signingKey;

    private static final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes)");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(final String userId, final long validityInSeconds, final String phoneNumber) {
        final Instant now = Instant.now();
        final Instant expiry = now.plusSeconds(validityInSeconds);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim(ApiConstants.PHONE_NUMBER, phoneNumber)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(final String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(extractJwt(token))
                .getBody();
    }

    public String getUserIdFromToken(final String token) {
        return validateToken(token).getSubject();
    }

    public static String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims decodeInternalJWTToken(String jwtToken) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    public Map<String, Object> decodeJwtWithoutVerification(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new IllegalArgumentException("JWT token cannot be null or empty");
            }
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token: must have 3 parts (header.payload.signature)");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});

            return payloadMap;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JWT payload: invalid JSON format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode JWT without verification", e);
        }
    }

    private static String extractJwt(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader;
        }
        return authorizationHeader.substring(7).trim();
    }

}
