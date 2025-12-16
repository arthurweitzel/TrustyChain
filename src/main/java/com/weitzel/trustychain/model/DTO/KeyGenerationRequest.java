package com.weitzel.trustychain.model.DTO;

public record KeyGenerationRequest(
        String algorithm,
        String keySize,
        String platform
) {}