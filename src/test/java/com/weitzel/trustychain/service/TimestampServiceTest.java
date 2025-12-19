package com.weitzel.trustychain.service;

import com.weitzel.trustychain.common.service.SignedTimestamp;
import com.weitzel.trustychain.common.service.TimestampService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimestampServiceTest {

    private TimestampService timestampService;

    @BeforeEach
    void setUp() throws Exception {
        timestampService = new TimestampService();

        // Generate keys and inject them
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        ReflectionTestUtils.setField(timestampService, "privateKey", keyPair.getPrivate());
        ReflectionTestUtils.setField(timestampService, "publicKey", keyPair.getPublic());
    }

    @Test
    @DisplayName("Should sign timestamp with data hash")
    void shouldSignTimestamp() {
        String dataHash = "abc123def456";

        SignedTimestamp result = timestampService.signTimestamp(dataHash);

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertNotNull(result.signature());
        assertTrue(result.timestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should verify valid signed timestamp")
    void shouldVerifyValidTimestamp() {
        String dataHash = "testHash123";

        SignedTimestamp signedTimestamp = timestampService.signTimestamp(dataHash);
        boolean valid = timestampService.verifyTimestamp(dataHash, signedTimestamp);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should reject timestamp with wrong hash")
    void shouldRejectTimestampWithWrongHash() {
        String originalHash = "originalHash";
        String wrongHash = "wrongHash";

        SignedTimestamp signedTimestamp = timestampService.signTimestamp(originalHash);
        boolean valid = timestampService.verifyTimestamp(wrongHash, signedTimestamp);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject timestamp with tampered signature")
    void shouldRejectTamperedSignature() {
        String dataHash = "hash123";
        SignedTimestamp originalTimestamp = timestampService.signTimestamp(dataHash);

        // Tamper with signature
        SignedTimestamp tamperedTimestamp = new SignedTimestamp(
                originalTimestamp.timestamp(),
                "dGFtcGVyZWQ=" // "tampered" in base64
        );

        boolean valid = timestampService.verifyTimestamp(dataHash, tamperedTimestamp);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should generate public key PEM")
    void shouldGeneratePublicKeyPem() {
        String pem = timestampService.getPublicKeyPem();

        assertNotNull(pem);
        assertTrue(pem.contains("-----BEGIN PUBLIC KEY-----"));
        assertTrue(pem.contains("-----END PUBLIC KEY-----"));
    }
}
