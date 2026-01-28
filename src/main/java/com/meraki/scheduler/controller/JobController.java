package com.meraki.scheduler.controller;

import com.meraki.scheduler.dto.CreateJobRequest;
import com.meraki.scheduler.dto.Jobs;
import com.meraki.scheduler.service.JobService;
import com.meraki.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/jobs")
@Tag(name = "Job Controller", description = "Controller for job system")
public class JobController {

    @Autowired
    JobService jobService;

    @Autowired
    AuthService authService;

    @Operation(summary = "Function to create job for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping
    public ResponseEntity<Jobs> createJob(@RequestHeader("Authorization") String token,
                                          @RequestBody CreateJobRequest request) {

        String authId = authService.validateTokenAndGetUserId(token);
        return ResponseEntity.ok().body(jobService.createJob(authId, request));

    }

    @Operation(summary = "Function to cancel job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> cancelJob(@RequestHeader("Authorization") String token,
                                          @PathVariable("jobId") String jobId) {

        String userId = authService.validateTokenAndGetUserId(token);
        jobService.cancelJob(userId, jobId);
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Function to get job for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<List<Jobs>> getJobs(@RequestHeader("Authorization") String token) {

        String authId = authService.validateTokenAndGetUserId(token);
        return ResponseEntity.ok().body(jobService.getJobList(authId));

    }
}