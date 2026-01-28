package com.meraki.workerNode.service;

import com.meraki.constants.Enums;
import com.meraki.utils.DateTimeUtils;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.repository.TaskEventSourcingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class IdempotencyValidationService {

    @Autowired
    TaskEventSourcingRepository taskEventSourcingRepository;

    public boolean validateTaskIdempotency(TaskMessage taskMessage) {

        Instant now = DateTimeUtils.currentTimestamp();
        Duration duration = Duration.between(Instant.ofEpochSecond(taskMessage.getCreatedAt().getTime()), now);
        if(duration.getSeconds() < 24 * 60 * 60) {

            // In case the message has come for reprocessing within 24 hours, check the last processing status
            Optional<Map<String, AttributeValue>> latestTaskEntry = taskEventSourcingRepository.fetchLatestTaskEntry(taskMessage.getTaskId());
            if (latestTaskEntry.isPresent()) {
                String status = latestTaskEntry.get().get("status").s();
                if (!isTerminalStatus(status)) {
                    return true;
                } else {
                    log.info("Task {} already in terminal state: {}", taskMessage.getTaskId(), status);
                }
            }
            return true;

        } else {
            taskEventSourcingRepository.updateStatus(taskMessage, Enums.TaskStatus.TIMED_OUT);
        }

        return false;

    }

    private boolean isTerminalStatus(String status) {
        return !(status.equals(Enums.TaskStatus.SCHEDULED.name())
                || status.equals(Enums.TaskStatus.PROCESSING.name()));
    }
}