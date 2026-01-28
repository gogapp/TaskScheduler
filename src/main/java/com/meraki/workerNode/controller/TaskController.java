package com.meraki.workerNode.controller;

import com.meraki.scheduler.dto.Jobs;
import com.meraki.user.service.AuthService;
import com.meraki.workerNode.dto.Task;
import com.meraki.workerNode.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/task")
@Tag(name = "Task Controller", description = "Controller for task system")
public class TaskController {

    @Autowired
    TaskService taskService;

    @Autowired
    AuthService authService;

    @Operation(summary = "Function to get task for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<List<Task>> getJobs(@RequestHeader("Authorization") String token) {

        String authId = authService.validateTokenAndGetUserId(token);
        return ResponseEntity.ok().body(taskService.getTaskList(authId));

    }

}
