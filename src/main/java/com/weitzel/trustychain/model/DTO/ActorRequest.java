package com.weitzel.trustychain.model.DTO;

public record ActorRequest(
        String name,
        String role,
        String publicKey
) {}
