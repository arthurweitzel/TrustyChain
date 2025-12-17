package com.weitzel.trustychain.model.DTO;

/**
 * Request DTO for RSA key generation guidance.
 */
public record KeyGenerationRequest(
        String platform,    // Platform: "openssl", "java", "javascript", "python"
        String keySize,     // Key size: "2048", "3072", "4096"
        String useCase       // Use case: "signing", "encryption", "both"
) {
}