package com.meraki.scheduler.service;

import com.meraki.constants.Enums;
import com.meraki.scheduler.dto.CreateJobRequest;
import com.meraki.scheduler.dto.Jobs;
import com.meraki.scheduler.repository.JobRepository;
import com.meraki.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    @Autowired
    JobRepository jobRepository;

    public Jobs createJob(String authUid, CreateJobRequest req) {

        Jobs job = Jobs.builder()
                .jobId(UUID.randomUUID().toString())
                .userId(authUid)
                .curl(req.getCurl())
                .cron(req.getCron())
                .status(Enums.JobStatus.SCHEDULED.name())
                .nextStartTime(DateTimeUtils.getNextStartTime(Instant.now(), req.getCron()))
                .createdAt(Instant.now())
                .build();

        jobRepository.save(job);
        return job;

    }

    public List<Jobs> getJobList(String userId) {
        // TODO: Add cursor based pagination
        return jobRepository.getJobsByUserId(userId);
    }

    public void cancelJob(String userId, String jobId) {
        jobRepository.cancelJobId(userId, jobId);
    }
}
