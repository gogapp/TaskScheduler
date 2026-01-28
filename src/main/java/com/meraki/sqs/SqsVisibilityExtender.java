package com.meraki.sqs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Component
public class SqsVisibilityExtender {

    @Autowired
    private SqsClient sqsClient;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ScheduledFuture<?> startLease(String queueUrl, String receiptHandle,
            int visibilitySeconds, int extendEverySeconds) {

        return scheduler.scheduleAtFixedRate(() -> {
            try {
                sqsClient.changeMessageVisibility(
                        ChangeMessageVisibilityRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(receiptHandle)
                                .visibilityTimeout(visibilitySeconds)
                                .build()
                );
            } catch (Exception e) {
                // Log only — retry safety is handled by SQS
                System.err.println("Failed to extend visibility: " + e.getMessage());
            }
        }, extendEverySeconds, extendEverySeconds, TimeUnit.SECONDS);

    }
}
