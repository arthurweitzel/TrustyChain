package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.LoginRequest;
import com.weitzel.trustychain.model.DTO.LoginResponse;
import com.weitzel.trustychain.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
