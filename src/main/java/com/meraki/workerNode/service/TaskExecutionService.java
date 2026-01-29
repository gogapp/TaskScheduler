package com.meraki.workerNode.service;

import com.meraki.constants.Enums;
import com.meraki.sqs.SqsVisibilityExtender;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.repository.TaskEventSourcingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskExecutionService {

    @Value("${sqs.task.processing.queue.url}")
    String taskProcessingQueueUrl;

    @Autowired
    HttpTaskExecutor httpTaskExecutor;

    @Autowired
    IdempotencyValidationService idempotencyValidationService;

    @Autowired
    SqsVisibilityExtender sqsVisibilityExtender;

    @Autowired
    TaskEventSourcingRepository taskEventSourcingRepository;

    public void executeTaskSync(TaskMessage taskMessage) {

        try {

            if (!idempotencyValidationService.validateTaskIdempotency(taskMessage)) {
                return;
            }

            sqsVisibilityExtender.startLease(taskProcessingQueueUrl, taskMessage.getReceiptHandle(),
                    30, 30);

            log.info("Started processing taskMessage - {}", taskMessage);
            taskEventSourcingRepository.updateStatus(taskMessage, Enums.TaskStatus.PROCESSING);
            httpTaskExecutor.execute(taskMessage.getHttpRequest());
            taskEventSourcingRepository.updateStatus(taskMessage, Enums.TaskStatus.COMPLETED);
            log.info("Completed processing taskMessage - {}", taskMessage);

        } catch (Exception e) {
            log.error("Failed to execute taskMessage - {}", taskMessage, e);
            throw e;
        }

    }

}
