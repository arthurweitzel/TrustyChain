package com.weitzel.trustychain.service;

import com.weitzel.trustychain.common.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashServiceExtendedTest {

    private HashService hashService;

    @BeforeEach
    void setUp() {
        hashService = new HashService();
    }

    @Test
    @DisplayName("Hash should be deterministic")
    void hashShouldBeDeterministic() {
        String h1 = hashService.calculateIntegrityHash("a", "b", "c", "d", "e");
        String h2 = hashService.calculateIntegrityHash("a", "b", "c", "d", "e");
        String h3 = hashService.calculateIntegrityHash("a", "b", "c", "d", "e");

        assertEquals(h1, h2);
        assertEquals(h2, h3);
    }

    @Test
    @DisplayName("Different actor produces different hash")
    void differentActorProducesDifferentHash() {
        String h1 = hashService.calculateIntegrityHash(null, "actor1", "P", "E", "M");
        String h2 = hashService.calculateIntegrityHash(null, "actor2", "P", "E", "M");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Different product produces different hash")
    void differentProductProducesDifferentHash() {
        String h1 = hashService.calculateIntegrityHash(null, "A", "prod1", "E", "M");
        String h2 = hashService.calculateIntegrityHash(null, "A", "prod2", "E", "M");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Different event type produces different hash")
    void differentEventTypeProducesDifferentHash() {
        String h1 = hashService.calculateIntegrityHash(null, "A", "P", "CREATE", "M");
        String h2 = hashService.calculateIntegrityHash(null, "A", "P", "UPDATE", "M");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Different metadata produces different hash")
    void differentMetadataProducesDifferentHash() {
        String h1 = hashService.calculateIntegrityHash(null, "A", "P", "E", "meta1");
        String h2 = hashService.calculateIntegrityHash(null, "A", "P", "E", "meta2");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Hash should be 64 characters (SHA-256)")
    void hashShouldBe64Characters() {
        String hash = hashService.calculateIntegrityHash("prev", "actor", "prod", "event", "meta");
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Hash should be hexadecimal")
    void hashShouldBeHexadecimal() {
        String hash = hashService.calculateIntegrityHash("p", "a", "p", "e", "m");
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("Empty strings produce valid hash")
    void emptyStringsProduceValidHash() {
        String hash = hashService.calculateIntegrityHash(null, "", "", "", "");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Unicode characters produce valid hash")
    void unicodeCharactersProduceValidHash() {
        String hash = hashService.calculateIntegrityHash(null, "Açúcar", "Café", "生产", "日本語");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Long strings produce valid hash")
    void longStringsProduceValidHash() {
        String longStr = "x".repeat(10000);
        String hash = hashService.calculateIntegrityHash(longStr, longStr, longStr, longStr, longStr);
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
