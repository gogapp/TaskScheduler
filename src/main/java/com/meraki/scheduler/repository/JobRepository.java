package com.meraki.scheduler.repository;

import com.meraki.constants.Enums;
import com.meraki.scheduler.dto.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public class JobRepository {

    @Value("${dynamo.jobs.table}")
    String jobTable;

    @Value("${dynamo.jobs.start.time.index}")
    String jobStartTimeIndex;

    @Autowired
    DynamoDbClient dynamoDb;

    public void save(Jobs job) {

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(jobTable)
                .item(Map.of(
                        "status", AttributeValue.fromS(job.getStatus()),
                        "nextStartTime", AttributeValue.fromN(String.valueOf(job.getNextStartTime().getEpochSecond())),
                        "jobId", AttributeValue.fromS(job.getJobId()),
                        "userId", AttributeValue.fromS(job.getUserId()),
                        "curl", AttributeValue.fromS(job.getCurl()),
                        "cron", AttributeValue.fromS(job.getCron()),
                        "createdAt", AttributeValue.fromS(String.valueOf(job.getCreatedAt().getEpochSecond()))
                ))
                .build());
    }

    public List<Jobs> findJobsStartingBetween(long from, long to) {

        QueryRequest request = QueryRequest.builder()
                .tableName(jobTable)
                .indexName(jobStartTimeIndex)
                .keyConditionExpression(
                        "#st = :sb AND nextStartTime BETWEEN :from AND :to"
                )
                .expressionAttributeNames(Map.of(
                        "#st", "status"
                ))
                .expressionAttributeValues(Map.of(
                        ":sb", AttributeValue.fromS(Enums.JobStatus.SCHEDULED.name()),
                        ":from", AttributeValue.fromN(String.valueOf(from)),
                        ":to", AttributeValue.fromN(String.valueOf(to))
                ))
                .build();

        return dynamoDb.query(request)
                .items()
                .stream()
                .map(this::map)
                .toList();
    }

    public List<Jobs> getJobsByUserId(String userId) {

        QueryRequest request = QueryRequest.builder()
                .tableName(jobTable)
                .keyConditionExpression(
                        "userId = :userId"
                )
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.fromS(userId)
                ))
                .build();

        return dynamoDb.query(request)
                .items()
                .stream()
                .map(this::map)
                .toList();
    }

    public void cancelJobId(String userId, String jobId) {

        // 1️⃣ Query all jobs for userId and filter by jobId
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(jobTable)
                .keyConditionExpression("userId = :uid")
                .filterExpression("jobId = :jid")
                .expressionAttributeValues(Map.of(
                        ":uid", AttributeValue.fromS(userId),
                        ":jid", AttributeValue.fromS(jobId)
                ))
                .build();

        QueryResponse queryResponse = dynamoDb.query(queryRequest);

        if (queryResponse.items().isEmpty()) {
            return; // job not found
        }

        Map<String, AttributeValue> item = queryResponse.items().get(0);
        String createdAt = item.get("createdAt").s();

        // 2️⃣ Update the job status to EXPIRED
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(jobTable)
                .key(Map.of(
                        "userId", AttributeValue.fromS(userId),
                        "createdAt", AttributeValue.fromS(createdAt)
                ))
                .updateExpression("SET #st = :expired, updatedAt = :now")
                .expressionAttributeNames(Map.of(
                        "#st", "status"
                ))
                .expressionAttributeValues(Map.of(
                        ":expired", AttributeValue.fromS(Enums.JobStatus.EXPIRED.name()),
                        ":now", AttributeValue.fromS(String.valueOf(System.currentTimeMillis()))
                ))
                .build();

        dynamoDb.updateItem(updateRequest);
    }

    public void updateNextStartTime(String userId, Instant createdAt, Instant next) {

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(jobTable)
                .key(Map.of(
                        "userId", AttributeValue.fromS(userId),
                        "createdAt", AttributeValue.fromS(String.valueOf(createdAt.toEpochMilli()))
                ))
                .updateExpression("SET nextStartTime = :next, updatedAt = :now")
                .expressionAttributeValues(Map.of(
                        ":next", AttributeValue.fromN(String.valueOf(next.toEpochMilli())),
                        ":now", AttributeValue.fromS(String.valueOf(Instant.now().toEpochMilli()))
                ))
                .build();

        dynamoDb.updateItem(request);
    }

    private Jobs map(Map<String, AttributeValue> item) {
        return Jobs.builder()
                .jobId(item.get("jobId").s())
                .userId(item.get("userId").s())
                .curl(item.get("curl").s())
                .cron(item.get("cron").s())
                .nextStartTime(Instant.ofEpochSecond(Long.parseLong(item.get("nextStartTime").n())))
                .createdAt(Instant.ofEpochSecond(Long.parseLong(item.get("createdAt").s())))
                .status(item.get("status").s())
                .build();
    }

}
