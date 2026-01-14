package com.weitzel.trustychain.tracking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TrackingResponse(
        String productCode,
        boolean isValid,
        List<ChainEventDTO> events,
        String qrCodeUrl) {

    public record ChainEventDTO(
            String actor,
            String eventType,
            String metadata,
            LocalDateTime timestamp,
            String currentHash) {}
}