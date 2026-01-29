package com.meraki.idgen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.HashMap;
import java.util.Map;

public class IdGenService {

    @Autowired
    private DynamoDbClient ddb;

    @Value("${dynamo.id.gen.table}")
    private String idGenTable;

    public long getNextId() {
        // Define the key for your counter item
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s("current_id").build());

        // Define expression attributes
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":inc", AttributeValue.builder().n("1").build());
        expressionAttributeValues.put(":start", AttributeValue.builder().n("0").build());

        // Update the item and return the new value
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(idGenTable)
                .key(key)
                // if_not_exists handles the first run by setting missing values to 0 before adding 1
                .updateExpression("SET val = if_not_exists(val, :start) + :inc")
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        UpdateItemResponse response = ddb.updateItem(updateRequest);

        // Extract and return the incremented long value
        return Long.parseLong(response.attributes().get("val").n());
    }
}
