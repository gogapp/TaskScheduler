package com.meraki.workerNode.dto;

import lombok.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMessage {

    private String jobId;
    private String taskId;
    private String userId;
    private TaskHttpRequest httpRequest;
    private UserQuota userQuota;
    private Timestamp startTime;
    private Timestamp createdAt;
    private String receiptHandle;

}
