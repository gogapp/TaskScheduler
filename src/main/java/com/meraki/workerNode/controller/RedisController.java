package com.meraki.workerNode.controller;

import com.meraki.user.service.AuthService;
import com.meraki.workerNode.dto.QuotaAndRateLimitSummary;
import com.meraki.workerNode.iface.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/monitor/redis")
@Tag(name = "Redis Monitoring Controller", description = "Controller for redis monitoring")
public class RedisController {

    @Autowired
    AuthService authService;

    @Autowired
    MonitoringService monitoringService;

    @Operation(summary = "Function for summarized view of user limits and quotas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<QuotaAndRateLimitSummary> getLimitsAndQuotas(@RequestHeader("Authorization") String token) {

        String authId = authService.validateTokenAndGetUserId(token);
        return ResponseEntity.ok().body(monitoringService.getQuotasAndRateLimitSummary(authId));

    }

}
