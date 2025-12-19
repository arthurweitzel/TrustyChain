package com.weitzel.trustychain.service;

import com.weitzel.trustychain.common.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashServiceTest {

    private HashService hashService;

    @BeforeEach
    void setUp() {
        hashService = new HashService();
    }

    @Test
    @DisplayName("Should calculate integrity hash with all parameters")
    void shouldCalculateIntegrityHash() {
        String hash = hashService.calculateIntegrityHash(
                "previousHash123",
                "Fazenda Santa Clara",
                "CAFE-001",
                "COLHEITA",
                "Lote 500kg");

        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 = 64 hex chars
        assertTrue(hash.matches("[a-f0-9]+"));
    }

    @Test
    @DisplayName("Should return consistent hash for same input")
    void shouldReturnConsistentHash() {
        String hash1 = hashService.calculateIntegrityHash(
                "prev", "actor", "product", "event", "meta");
        String hash2 = hashService.calculateIntegrityHash(
                "prev", "actor", "product", "event", "meta");

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should handle null previous hash with BEGIN prefix")
    void shouldHandleNullPreviousHash() {
        String hash = hashService.calculateIntegrityHash(
                null, "actor", "PROD-001", "CREATE", "metadata");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should handle null metadata")
    void shouldHandleNullMetadata() {
        String hash = hashService.calculateIntegrityHash(
                "prevHash", "actor", "PROD-001", "CREATE", null);

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Different inputs should produce different hashes")
    void shouldProduceDifferentHashesForDifferentInputs() {
        String hash1 = hashService.calculateIntegrityHash(
                null, "actor1", "PROD-001", "CREATE", "meta");
        String hash2 = hashService.calculateIntegrityHash(
                null, "actor2", "PROD-001", "CREATE", "meta");

        assertNotEquals(hash1, hash2);
    }
}
