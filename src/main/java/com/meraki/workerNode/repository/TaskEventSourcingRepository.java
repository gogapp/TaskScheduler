package com.meraki.workerNode.repository;

import com.meraki.idgen.IdGenService;
import com.meraki.scheduler.dto.Jobs;
import com.meraki.workerNode.dto.Task;
import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.utils.DateTimeUtils;
import com.meraki.constants.Enums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskEventSourcingRepository {

    @Autowired
    private DynamoDbClient dynamoDb;

    @Value("${dynamo.task.table}")
    String tableName;

    @Value("${dynamo.task.entry.index}")
    String taskEntryIndex;

    @Autowired
    IdGenService idGenService;

    public Optional<Map<String, AttributeValue>> fetchLatestTaskEntry(String taskId) {

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName(taskEntryIndex)
                .keyConditionExpression("taskId = :taskId")
                .expressionAttributeValues(Map.of(
                        ":taskId", AttributeValue.fromS(taskId)
                ))
                .scanIndexForward(false) // DESC order (latest first)
                .limit(1)
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);

        if (response.count() == 0) {
            return Optional.empty();
        }

        return Optional.of(response.items().get(0));
    }

    public void updateStatus(TaskMessage taskMessage, Enums.TaskStatus taskStatus) {
        long id = idGenService.getNextId();
        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(tableName)
                        .item(Map.of(
                                "id", AttributeValue.fromN(String.valueOf(id)),
                                "taskId", AttributeValue.fromS(taskMessage.getTaskId()),
                                "jobId", AttributeValue.fromS(taskMessage.getJobId()),
                                "userId", AttributeValue.fromS(taskMessage.getUserId()),
                                "httpRequest", AttributeValue.fromS(String.valueOf(taskMessage.getHttpRequest())),
                                "status", AttributeValue.fromS(taskStatus.name()),
                                "startTime", AttributeValue.fromS(String.valueOf(taskMessage.getStartTime().getTime())),
                                "createdAt", AttributeValue.fromS(String.valueOf(DateTimeUtils.currentTimestamp().getEpochSecond() * 1000))
                        ))
                        .build()
        );
    }

    public List<Task> getTaskList(String userId) {

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.fromS(userId)
                ))
                .build();

        return dynamoDb.query(queryRequest)
                .items()
                .stream()
                .map(this::map)
                .toList();

    }

    private Task map(Map<String, AttributeValue> item) {
        return Task.builder()
                .taskId(item.get("taskId").s())
                .jobId(item.get("jobId").s())
                .userId(item.get("userId").s())
                .status(item.get("status").s())
                .startTime(new Timestamp(Long.parseLong(item.get("startTime").s())))
                .createdAt(new Timestamp(Long.parseLong(item.get("createdAt").s())))
                .build();
    }

}
