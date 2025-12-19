package com.weitzel.trustychain.service;

import com.weitzel.trustychain.common.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private CryptoService cryptoService;
    private KeyPair testKeyPair;
    private String publicKeyPem;

    @BeforeEach
    void setUp() throws Exception {
        cryptoService = new CryptoService();

        // Generate test key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        testKeyPair = keyGen.generateKeyPair();

        // Convert to PEM format
        String base64 = Base64.getEncoder().encodeToString(testKeyPair.getPublic().getEncoded());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length()));
            pem.append("\n");
        }
        pem.append("-----END PUBLIC KEY-----");
        publicKeyPem = pem.toString();
    }

    @Test
    @DisplayName("Should load public key from PEM format")
    void shouldLoadPublicKeyFromPem() {
        PublicKey loaded = cryptoService.loadPublicKeyFromPem(publicKeyPem);

        assertNotNull(loaded);
        assertEquals("RSA", loaded.getAlgorithm());
        assertArrayEquals(testKeyPair.getPublic().getEncoded(), loaded.getEncoded());
    }

    @Test
    @DisplayName("Should verify valid signature")
    void shouldVerifyValidSignature() throws Exception {
        byte[] data = "test data to sign".getBytes(StandardCharsets.UTF_8);

        // Sign with private key
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(testKeyPair.getPrivate());
        signer.update(data);
        String signatureBase64 = Base64.getEncoder().encodeToString(signer.sign());

        // Verify with public key
        PublicKey publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        boolean valid = cryptoService.verifySignature(data, signatureBase64, publicKey);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should reject invalid signature")
    void shouldRejectInvalidSignature() {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        String invalidSignature = Base64.getEncoder().encodeToString("invalid".getBytes());

        PublicKey publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        boolean valid = cryptoService.verifySignature(data, invalidSignature, publicKey);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject signature for different data")
    void shouldRejectSignatureForDifferentData() throws Exception {
        byte[] originalData = "original data".getBytes(StandardCharsets.UTF_8);
        byte[] modifiedData = "modified data".getBytes(StandardCharsets.UTF_8);

        // Sign original data
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(testKeyPair.getPrivate());
        signer.update(originalData);
        String signatureBase64 = Base64.getEncoder().encodeToString(signer.sign());

        // Verify with modified data
        PublicKey publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        boolean valid = cryptoService.verifySignature(modifiedData, signatureBase64, publicKey);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should throw exception for invalid PEM")
    void shouldThrowExceptionForInvalidPem() {
        String invalidPem = "-----BEGIN PUBLIC KEY-----\ninvalid\n-----END PUBLIC KEY-----";

        assertThrows(RuntimeException.class, () -> cryptoService.loadPublicKeyFromPem(invalidPem));
    }
}
