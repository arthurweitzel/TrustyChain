package com.weitzel.trustychain.service;

import com.weitzel.trustychain.tracking.TrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TrackingServiceTest {

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService();
        ReflectionTestUtils.setField(trackingService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("Should generate QR code as PNG bytes")
    void shouldGenerateQRCode() {
        byte[] qrCode = trackingService.generateQRCode("PROD-001");

        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
        // PNG magic bytes
        assertEquals((byte) 0x89, qrCode[0]);
        assertEquals((byte) 0x50, qrCode[1]); // 'P'
        assertEquals((byte) 0x4E, qrCode[2]); // 'N'
        assertEquals((byte) 0x47, qrCode[3]); // 'G'
    }

    @Test
    @DisplayName("Should generate QR code with custom size")
    void shouldGenerateQRCodeWithCustomSize() {
        byte[] qrCode = trackingService.generateQRCode("PROD-001", 200, 200);

        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
    }

    @Test
    @DisplayName("Should generate tracking URL")
    void shouldGenerateTrackingUrl() {
        String url = trackingService.generateTrackingUrl("CAFE-2024-001");

        assertEquals("http://localhost:8080/api/tracking/CAFE-2024-001", url);
    }

    @Test
    @DisplayName("Should generate QR code URL")
    void shouldGenerateQRCodeUrl() {
        String url = trackingService.generateQRCodeUrl("CAFE-2024-001");

        assertEquals("http://localhost:8080/api/tracking/CAFE-2024-001/qr", url);
    }

    @Test
    @DisplayName("Should use configured base URL")
    void shouldUseConfiguredBaseUrl() {
        ReflectionTestUtils.setField(trackingService, "baseUrl", "https://api.trustychain.com");

        String url = trackingService.generateTrackingUrl("PROD-001");

        assertEquals("https://api.trustychain.com/api/tracking/PROD-001", url);
    }
}
