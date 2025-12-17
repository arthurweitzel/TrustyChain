package com.weitzel.trustychain.model.DTO;

/**
 * Response DTO for RSA key generation guidance from AI.
 */
public record KeyGenerationResponse(
        String explanation,
        String command,
        String[] steps,
        String securityNote,
        String exampleOutput
) {
}