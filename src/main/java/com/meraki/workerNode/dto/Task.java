package com.meraki.workerNode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    private String taskId;
    private String jobId;
    private String userId;
    private String httpRequest;
    private String status;
    private Timestamp startTime;
    private Timestamp createdAt;

}
