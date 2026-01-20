package com.weitzel.trustychain.auth;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorService;
import com.weitzel.trustychain.auth.dto.LoginRequest;
import com.weitzel.trustychain.auth.dto.LoginResponse;
import com.weitzel.trustychain.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ActorService actorService;

    public AuthenticationController(AuthenticationService authenticationService, ActorService actorService) {
        this.authenticationService = authenticationService;
        this.actorService = actorService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Actor> register(@Valid @RequestBody RegisterRequest request) {
        Actor actor = actorService.registerActor(
                request.name(),
                request.username(),
                request.password(),
                "ROLE_USER",
                request.publicKey());
        return ResponseEntity.ok(actor);
    }
}