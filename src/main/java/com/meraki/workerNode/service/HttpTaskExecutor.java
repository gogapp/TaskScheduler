package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.TaskHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpTaskExecutor {

    @Autowired
    RestTemplate restTemplate;

    public void execute(TaskHttpRequest msg) {

        HttpHeaders headers = new HttpHeaders();
        if(msg.getHeaders() != null && !msg.getHeaders().isEmpty()) {
            msg.getHeaders().forEach(headers::add);
        }

        HttpEntity<String> entity = new HttpEntity<>(msg.getBody(), headers);

        // TODO: timeout implementation
        restTemplate.exchange(
                msg.getUrl(),
                HttpMethod.valueOf(msg.getMethod()),
                entity,
                String.class
        );
    }
}

