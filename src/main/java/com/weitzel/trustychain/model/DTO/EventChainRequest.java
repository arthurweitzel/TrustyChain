package com.weitzel.trustychain.model.DTO;

public record EventChainRequest (
    String productCode,
    String actor,
    String eventType,
    String metadata,
    String signature // base64-encoded signature of the event payload
) {}


