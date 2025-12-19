package com.weitzel.trustychain.common.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service for generating and verifying signed timestamps.
 * Uses RSA keys to sign timestamps, providing proof of when events occurred.
 */
@Service
public class TimestampService {
    private static final Logger log = LoggerFactory.getLogger(TimestampService.class);
    private static final String ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    @Value("${trustychain.timestamp.private-key-path:}")
    private String privateKeyPath;

    @Value("${trustychain.timestamp.public-key-path:}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            if (privateKeyPath != null && !privateKeyPath.isEmpty()
                    && publicKeyPath != null && !publicKeyPath.isEmpty()) {
                loadKeysFromFiles();
            } else {
                generateNewKeyPair();
            }
        } catch (Exception e) {
            log.error("Failed to initialize timestamp keys, generating new pair", e);
            generateNewKeyPair();
        }
    }

    /**
     * Signs the current timestamp along with the data hash.
     * The signature proves that the data existed at this specific time.
     *
     * @param dataHash the hash of the data being timestamped
     * @return a SignedTimestamp containing the timestamp and its signature
     */
    public SignedTimestamp signTimestamp(String dataHash) {
        // Truncate to microseconds to match PostgreSQL precision
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        String dataToSign = dataHash + "|" + timestamp.toString();

        try {
            Signature signer = Signature.getInstance(ALGORITHM);
            signer.initSign(privateKey);
            signer.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signer.sign();
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

            return new SignedTimestamp(timestamp, signatureBase64);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign timestamp", e);
        }
    }

    /**
     * Verifies that a signed timestamp is authentic and was signed by this server.
     *
     * @param dataHash        the original data hash
     * @param signedTimestamp the timestamp with signature to verify
     * @return true if the signature is valid
     */
    public boolean verifyTimestamp(String dataHash, SignedTimestamp signedTimestamp) {
        String dataToVerify = dataHash + "|" + signedTimestamp.timestamp().toString();

        try {
            Signature verifier = Signature.getInstance(ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signedTimestamp.signature());
            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to verify timestamp signature", e);
            return false;
        }
    }

    /**
     * Returns the public key in PEM format for external verification.
     */
    public String getPublicKeyPem() {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length()));
            pem.append("\n");
        }
        pem.append("-----END PUBLIC KEY-----");
        return pem.toString();
    }

    private void loadKeysFromFiles() throws Exception {
        // Load private key
        String privateKeyPem = Files.readString(Path.of(privateKeyPath));
        String privateKeyBase64 = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(privateSpec);

        // Load public key
        String publicKeyPem = Files.readString(Path.of(publicKeyPath));
        String publicKeyBase64 = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = keyFactory.generatePublic(publicSpec);

        log.info("Loaded timestamp keys from files");
    }

    private void generateNewKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            KeyPair keyPair = generator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            log.warn("Generated new timestamp key pair. For production, configure persistent keys.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate timestamp key pair", e);
        }
    }
}
