package com.weitzel.trustychain.model.DTO;

public record EventChainRequest (
    String productCode,
    String actor,
    String eventType,
    String metadata
) {}


