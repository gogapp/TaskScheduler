package com.meraki.workerNode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskHttpRequest {

    private String method;          // GET, POST, PUT, DELETE
    private String url;
    private Map<String, String> headers;
    private String body;             // JSON / text
    private int timeoutSeconds;

}
