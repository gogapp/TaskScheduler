package com.meraki.user.repository;

import com.meraki.user.dto.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    @Value("${dynamo.user.table}")
    String userTable;

    @Value("${dynamo.user.phoneNumber.index}")
    String userPhoneNumberIndex;

    @Autowired
    DynamoDbClient dynamoDb;

    public void saveUser(Users user) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(userTable)
                .item(Map.of(
                        "userId", AttributeValue.fromS(user.getUserId()),
                        "phoneNumber", AttributeValue.fromS(user.getPhoneNumber()),
                        "createdAt", AttributeValue.fromS(String.valueOf(user.getCreatedAt()))
                ))
                .build());
    }

    public boolean userExists(String userId) {

        QueryRequest request = QueryRequest.builder()
                .tableName(userTable)
                .keyConditionExpression("userId = :uid")
                .expressionAttributeValues(Map.of(
                        ":uid", AttributeValue.fromS(userId)
                ))
                .limit(1)
                .build();

        QueryResponse response = dynamoDb.query(request);
        return response.count() > 0;

    }

    public Optional<Users> getUserByPhoneNumber(String phoneNumber) {

        try {

            QueryRequest request = QueryRequest.builder()
                    .tableName(userTable)
                    .indexName(userPhoneNumberIndex)
                    .keyConditionExpression("phoneNumber = :phone")
                    .expressionAttributeValues(Map.of(
                            ":phone", AttributeValue.fromS(phoneNumber)
                    ))
                    .limit(1)                 // expect 1 user
                    .scanIndexForward(false)  // latest createdAt first
                    .build();

            QueryResponse response = dynamoDb.query(request);
            if(response.items().isEmpty()) {
                return Optional.empty();
            }

            Map<String, AttributeValue> item = response.items().get(0);
            if (item.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapToUser(item));

        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }

    }

    private Users mapToUser(Map<String, AttributeValue> item) {
        return new Users(
                item.get("userId").s(),
                item.get("phoneNumber").s(),
                Long.parseLong(item.get("createdAt").s())
        );
    }
}