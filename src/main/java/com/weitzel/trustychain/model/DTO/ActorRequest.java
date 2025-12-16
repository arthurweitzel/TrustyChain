package com.weitzel.trustychain.model.DTO;

public record ActorRequest(
        String name,
        String username,
        String password,
        String role,
        String publicKey
) {}
