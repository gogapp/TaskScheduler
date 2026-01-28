package com.meraki.user.controller;

import com.meraki.user.dto.RegisterUserRequest;
import com.meraki.user.dto.TokenResponse;
import com.meraki.user.dto.Users;
import com.meraki.user.dto.ValidateUserResponse;
import com.meraki.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Controller", description = "Controller for user system")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 1. Register user using phone number
     */
    @Operation(summary = "Function to register user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterUserRequest request) {
        return ResponseEntity.ok().body(userService.registerUser(request.getPhoneNumber()));
    }

    /**
     * 2. Validate if userId exists
     */
    @Operation(summary = "Function to validate authToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400",  description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/validate")
    public ResponseEntity<ValidateUserResponse> validateAndGetUserId(
            @RequestHeader("Authorization") String authToken) {

        boolean exists = userService.validateUserToken(authToken);
        return ResponseEntity.ok(new ValidateUserResponse(exists));

    }
}
