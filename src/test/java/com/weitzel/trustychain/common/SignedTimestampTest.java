package com.weitzel.trustychain.common;

import com.weitzel.trustychain.common.service.SignedTimestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SignedTimestampTest {

    @Test
    @DisplayName("Should create SignedTimestamp")
    void shouldCreateSignedTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        SignedTimestamp ts = new SignedTimestamp(now, "signature123");

        assertEquals(now, ts.timestamp());
        assertEquals("signature123", ts.signature());
    }

    @Test
    @DisplayName("Should test equals")
    void shouldTestEquals() {
        LocalDateTime now = LocalDateTime.now();
        SignedTimestamp ts1 = new SignedTimestamp(now, "sig");
        SignedTimestamp ts2 = new SignedTimestamp(now, "sig");

        assertEquals(ts1, ts2);
    }

    @Test
    @DisplayName("Should test hashCode")
    void shouldTestHashCode() {
        LocalDateTime now = LocalDateTime.now();
        SignedTimestamp ts1 = new SignedTimestamp(now, "sig");
        SignedTimestamp ts2 = new SignedTimestamp(now, "sig");

        assertEquals(ts1.hashCode(), ts2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        LocalDateTime now = LocalDateTime.now();
        SignedTimestamp ts = new SignedTimestamp(now, "sig");

        assertNotNull(ts.toString());
    }

    @Test
    @DisplayName("Should handle null timestamp")
    void shouldHandleNullTimestamp() {
        SignedTimestamp ts = new SignedTimestamp(null, "sig");
        assertNull(ts.timestamp());
    }

    @Test
    @DisplayName("Should handle null signature")
    void shouldHandleNullSignature() {
        SignedTimestamp ts = new SignedTimestamp(LocalDateTime.now(), null);
        assertNull(ts.signature());
    }
}
