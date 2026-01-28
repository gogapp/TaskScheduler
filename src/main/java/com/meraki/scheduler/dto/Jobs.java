package com.meraki.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jobs {

    private String jobId;
    private String userId;
    private String cron;
    private String curl;
    private String status;
    private Instant nextStartTime;
    private Instant createdAt;

}
