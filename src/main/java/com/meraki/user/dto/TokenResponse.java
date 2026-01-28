package com.meraki.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class TokenResponse implements Serializable {
    private String accessToken;
    private String userId;
    private Timestamp accessExpiry;
}