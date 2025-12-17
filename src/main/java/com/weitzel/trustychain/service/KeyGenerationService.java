package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.model.DTO.KeyGenerationResponse;
import org.springframework.stereotype.Service;

/**
 * Service for providing RSA key generation guidance.
 * Focuses on secure key generation practices for different platforms.
 */
@Service
public class KeyGenerationService {

    /**
     * Generates RSA key generation guidance based on user's platform and requirements.
     */
    public KeyGenerationResponse getKeyGenerationGuidance(KeyGenerationRequest request) {
        return buildResponse(request);
    }

    /**
     * Builds comprehensive response for RSA key generation guidance.
     */
    private KeyGenerationResponse buildResponse(KeyGenerationRequest request) {
        String platform = request.platform() != null ? request.platform().toLowerCase() : "openssl";
        String keySize = request.keySize() != null ? request.keySize() : "2048";
        String useCase = request.useCase() != null ? request.useCase() : "signing";

        return new KeyGenerationResponse(
            generateExplanation(platform, keySize, useCase),
            generateCommand(platform, keySize),
            generateSteps(platform),
            generateSecurityNote(platform),
            generateExampleOutput(platform)
        );
    }

    private String generateExplanation(String platform, String keySize, String useCase) {
        return String.format(
            "This guide will help you generate a secure %s-bit RSA key pair using %s. " +
            "The key will be suitable for %s and follows current security best practices.",
            keySize, platform, useCase
        );
    }

    private String generateCommand(String platform, String keySize) {
        switch (platform) {
            case "openssl":
                return String.format("openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:%s", keySize);
            case "java":
                return String.format(
                    "KeyPairGenerator keyGen = KeyPairGenerator.getInstance(\"RSA\");\n" +
                    "keyGen.initialize(%s);\n" +
                    "KeyPair keyPair = keyGen.generateKeyPair();", keySize);
            case "python":
                return String.format(
                    "from cryptography.hazmat.primitives.asymmetric import rsa\n" +
                    "from cryptography.hazmat.primitives import serialization\n\n" +
                    "private_key = rsa.generate_private_key(\n" +
                    "    public_exponent=65537,\n" +
                    "    key_size=%s\n" +
                    ")", keySize);
            case "javascript":
                return String.format(
                    "const crypto = require('crypto');\n" +
                    "const { privateKey, publicKey } = crypto.generateKeyPairSync('rsa', {\n" +
                    "  modulusLength: %s,\n" +
                    "  publicKeyEncoding: { type: 'spki', format: 'pem' },\n" +
                    "  privateKeyEncoding: { type: 'pkcs8', format: 'pem' }\n" +
                    "});", keySize);
            default:
                return "Platform-specific command will be provided based on your requirements";
        }
    }

    private String[] generateSteps(String platform) {
        switch (platform) {
            case "openssl":
                return new String[]{
                    "1. Install OpenSSL if not already installed",
                    "2. Open your terminal or command prompt",
                    "3. Run the OpenSSL command with your desired key size",
                    "4. Protect the private key file with appropriate permissions",
                    "5. Extract the public key if needed using: openssl rsa -in private_key.pem -pubout -out public_key.pem"
                };
            case "java":
                return new String[]{
                    "1. Add Java Cryptography Extension (JCE) to your project",
                    "2. Create a KeyPairGenerator instance for RSA",
                    "3. Initialize with desired key size (2048+ recommended)",
                    "4. Generate the key pair",
                    "5. Store keys securely using appropriate encoding"
                };
            case "python":
                return new String[]{
                    "1. Install cryptography library: pip install cryptography",
                    "2. Import required modules from cryptography",
                    "3. Use rsa.generate_private_key() with desired key size",
                    "4. Serialize keys to PEM format for storage",
                    "5. Store private key securely and share public key"
                };
            case "javascript":
                return new String[]{
                    "1. Install Node.js if not already installed",
                    "2. Use Node.js built-in crypto module",
                    "3. Call generateKeyPairSync with RSA parameters",
                    "4. Choose appropriate key encoding format",
                    "5. Store keys securely, especially the private key"
                };
            default:
                return new String[]{
                    "1. Choose a supported cryptographic library",
                    "2. Follow platform-specific security guidelines",
                    "3. Use minimum 2048-bit key size for security",
                    "4. Store private keys with proper protection",
                    "5. Test keys before production use"
                };
        }
    }

    private String generateSecurityNote(String platform) {
        return String.format(
            "For %s RSA key generation: Always use minimum 2048-bit keys (3072+ recommended for long-term security). " +
            "Never share private keys, use secure storage, and consider using a Hardware Security Module (HSM) " +
            "for production environments. Back up keys securely and implement proper key rotation policies.",
            platform
        );
    }

    private String generateExampleOutput(String platform) {
        switch (platform) {
            case "openssl":
                return "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKiwggSiAgEAAoIBAQC...\n-----END PRIVATE KEY-----";
            case "java":
                return "PublicKey: Sun RSA public key, 2048 bits\n" +
                       "modulus: 1234567890...\n" +
                       "public exponent: 65537";
            case "python":
                return "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKiwggSiAgEAAoIBAQC...\n-----END PRIVATE KEY-----";
            case "javascript":
                return "Public Key:\n-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----\n" +
                       "Private Key:\n-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBK...\n-----END PRIVATE KEY-----";
            default:
                return "Example output will depend on your chosen platform and tools.";
        }
    }
}