package com.meraki.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken implements Serializable {

    private String userId;

    private String token;

    private Instant issuedAt;

    private Instant expiryAt;

    private boolean valid = true;

}
