package com.meraki.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meraki.scheduler.dto.Jobs;
import com.meraki.utils.JsonUtils;
import com.meraki.workerNode.dto.TaskHttpRequest;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.dto.UserQuota;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Component
public class SqsPublisher {

    @Value("${sqs.task.processing.queue.url}")
    String queueUrl;

    @Autowired
    SqsClient sqsClient;

    public void publish(TaskMessage taskMessage, long delaySeconds) throws JsonProcessingException {

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .delaySeconds((int) Math.max(0, delaySeconds))
                .messageBody(JsonUtils.getJsonString(taskMessage))
                .build());

        log.info("Published taskMessage - {}", taskMessage);
    }
}
