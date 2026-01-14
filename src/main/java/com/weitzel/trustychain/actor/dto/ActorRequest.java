package com.weitzel.trustychain.actor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActorRequest(
        @NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
        String role,
        @NotBlank(message = "Public key is required") String publicKey) {}