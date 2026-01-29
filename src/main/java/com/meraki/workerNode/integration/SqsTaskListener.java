package com.meraki.workerNode.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meraki.constants.Enums;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.exception.ConcurrencyQuotaExceededException;
import com.meraki.workerNode.exception.RateLimitExceededException;
import com.meraki.workerNode.repository.TaskEventSourcingRepository;
import com.meraki.workerNode.service.ConcurrencyQuotaService;
import com.meraki.workerNode.service.DistributedLeaseService;
import com.meraki.workerNode.service.UserRateLimiter;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SqsTaskListener {

    @Autowired
    UserRateLimiter userRateLimiter;

    @Autowired
    ConcurrencyQuotaService concurrencyQuotaService;

    @Autowired
    DistributedLeaseService distributedLeaseService;

    @Autowired
    TaskEventSourcingRepository taskEventSourcingRepository;

    @SqsListener(value = "${sqs.task.processing.queue.name}")
    public void handleMessage(String payload, @Header(SqsHeaders.SQS_RECEIPT_HANDLE_HEADER) String receiptHandle) throws Exception {

        TaskMessage message = new ObjectMapper().readValue(payload, TaskMessage.class);
        message.setReceiptHandle(receiptHandle);
        log.info("Received taskMessage - {}", message);

        try {

            userRateLimiter.checkRateLimit(message);
            try {
                concurrencyQuotaService.tryAcquire(message);
                distributedLeaseService.processWithTaskLease(message);
            } finally {
                concurrencyQuotaService.release(message.getUserId());
            }

            // TODO: Cleanup on process crash
            log.info("Completed processing task message - {}", message);

        } catch (Exception e) {
            if(e instanceof ConcurrencyQuotaExceededException) {
                taskEventSourcingRepository.updateStatus(message, Enums.TaskStatus.QUOTA_EXCEEDED);
            } else if(e instanceof RateLimitExceededException) {
                taskEventSourcingRepository.updateStatus(message, Enums.TaskStatus.RATE_LIMITED);
            }
            log.error("Error while processing message - {}", payload, e);
            throw e;
        }

    }
}
