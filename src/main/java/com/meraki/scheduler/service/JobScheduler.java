package com.meraki.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meraki.constants.Enums;
import com.meraki.scheduler.dto.Jobs;
import com.meraki.scheduler.repository.JobRepository;
import com.meraki.sqs.SqsPublisher;
import com.meraki.utils.DateTimeUtils;
import com.meraki.utils.HttpUtils;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.dto.UserQuota;
import com.meraki.workerNode.repository.TaskEventSourcingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JobScheduler {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    TaskEventSourcingRepository taskEventSourcingRepository;

    @Autowired
    SqsPublisher sqsPublisher;

    @Scheduled(fixedRate = 60_000)
    public void scheduleJobs() {

        long startTime = Instant.MIN.getEpochSecond();
        long now = Instant.now().getEpochSecond();
        long fiveMinutesLater = now + 300;

        List<Jobs> jobs = jobRepository.findJobsStartingBetween(startTime, fiveMinutesLater);

        for (Jobs job : jobs) {
            try {
                scheduleJob(job);
            } catch (Exception e) {
                log.error("Failed to schedule job {}", job.getJobId(), e);
            }
        }
    }

    @Transactional
    public void scheduleJob(Jobs job) throws JsonProcessingException {

        String taskId = UUID.randomUUID().toString();
        Instant startTime = job.getNextStartTime();
        Instant now = Instant.now();

        TaskMessage taskMessage = TaskMessage.builder()
                .jobId(job.getJobId())
                .taskId(taskId)
                .userId(job.getUserId())
                .httpRequest(HttpUtils.parse(job.getCurl()))
                .userQuota(new UserQuota())
                .startTime(new Timestamp(startTime.getEpochSecond()))
                .createdAt(new Timestamp(now.getEpochSecond())).build();

        taskEventSourcingRepository.updateStatus(taskMessage, Enums.TaskStatus.SCHEDULED);

        Duration delaySeconds = Duration.between(now, startTime);
        sqsPublisher.publish(taskMessage, delaySeconds.getSeconds());

        Instant next = DateTimeUtils.getNextStartTime(startTime, job.getCron());
        jobRepository.updateNextStartTime(job.getUserId(), job.getCreatedAt(), next);

    }

}
