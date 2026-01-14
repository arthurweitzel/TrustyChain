package com.weitzel.trustychain.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@RestController
@RequestMapping("/api/key-generation")
@Tag(name = "Key Generation", description = "RSA key generation and signing")
public class KeyGenerationController {

    @GetMapping
    @Operation(summary = "Generate RSA key pair", description = "Generates a new 2048-bit RSA key pair in PEM format")
    public ResponseEntity<KeyPairResponse> generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            String publicKeyPem = formatToPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
            String privateKeyPem = formatToPem(keyPair.getPrivate().getEncoded(), "PRIVATE KEY");

            return ResponseEntity.ok(new KeyPairResponse(publicKeyPem, privateKeyPem));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    @PostMapping("/sign")
    @Operation(summary = "Sign data with private key", description = "Signs data using the provided private key")
    public ResponseEntity<SignatureResponse> signData(@RequestBody SignRequest request) {
        try {
            String privateKeyBase64 = request.privateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(
                    privateKeyBytes);
            java.security.PrivateKey privateKey = java.security.KeyFactory.getInstance("RSA").generatePrivate(keySpec);

            java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(request.data().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] signatureBytes = signer.sign();

            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);
            return ResponseEntity.ok(new SignatureResponse(signatureBase64));
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign data: " + e.getMessage(), e);
        }
    }

    private String formatToPem(byte[] encoded, String type) {
        String base64 = Base64.getEncoder().encodeToString(encoded);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(type).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length()));
            pem.append("\n");
        }
        pem.append("-----END ").append(type).append("-----");
        return pem.toString();
    }

    public record KeyPairResponse(String publicKey, String privateKey) {
    }

    public record SignRequest(String data, String privateKey) {
    }

    public record SignatureResponse(String signature) {
    }
}