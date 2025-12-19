package com.weitzel.trustychain.service;

import com.weitzel.trustychain.common.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceExtendedTest {

    private CryptoService cryptoService;
    private KeyPair keyPair;
    private String publicKeyPem;

    @BeforeEach
    void setUp() throws Exception {
        cryptoService = new CryptoService();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        String base64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
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
    @DisplayName("Should load key from PEM without whitespace")
    void shouldLoadKeyFromPemWithoutWhitespace() {
        String base64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String singleLinePem = "-----BEGIN PUBLIC KEY-----" + base64 + "-----END PUBLIC KEY-----";

        var key = cryptoService.loadPublicKeyFromPem(singleLinePem);
        assertNotNull(key);
    }

    @Test
    @DisplayName("Should verify signature with empty data")
    void shouldVerifySignatureWithEmptyData() throws Exception {
        byte[] data = "".getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(data);
        String sig = Base64.getEncoder().encodeToString(signer.sign());

        var publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        assertTrue(cryptoService.verifySignature(data, sig, publicKey));
    }

    @Test
    @DisplayName("Should verify signature with large data")
    void shouldVerifySignatureWithLargeData() throws Exception {
        byte[] data = new byte[100000];
        java.util.Arrays.fill(data, (byte) 'X');

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(data);
        String sig = Base64.getEncoder().encodeToString(signer.sign());

        var publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        assertTrue(cryptoService.verifySignature(data, sig, publicKey));
    }

    @Test
    @DisplayName("Should verify signature with unicode data")
    void shouldVerifySignatureWithUnicodeData() throws Exception {
        byte[] data = "日本語 中文 한국어".getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(data);
        String sig = Base64.getEncoder().encodeToString(signer.sign());

        var publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        assertTrue(cryptoService.verifySignature(data, sig, publicKey));
    }

    @Test
    @DisplayName("Should reject empty signature")
    void shouldRejectEmptySignature() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        var publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);

        assertFalse(cryptoService.verifySignature(data, "", publicKey));
    }

    @Test
    @DisplayName("Should reject null signature gracefully")
    void shouldRejectNullSignatureGracefully() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        var publicKey = cryptoService.loadPublicKeyFromPem(publicKeyPem);

        assertFalse(cryptoService.verifySignature(data, null, publicKey));
    }

    @Test
    @DisplayName("Same key should produce same encoding")
    void sameKeyShouldProduceSameEncoding() {
        var key1 = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        var key2 = cryptoService.loadPublicKeyFromPem(publicKeyPem);

        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
    }

    @Test
    @DisplayName("Loaded key should have RSA algorithm")
    void loadedKeyShouldHaveRsaAlgorithm() {
        var key = cryptoService.loadPublicKeyFromPem(publicKeyPem);
        assertEquals("RSA", key.getAlgorithm());
    }
}
