package com.weitzel.trustychain.chain.dto;

import jakarta.validation.constraints.NotBlank;

public record EventChainRequest(
        @NotBlank(message = "Product code is required") String productCode,

        @NotBlank(message = "Actor name is required") String actor,

        @NotBlank(message = "Event type is required") String eventType,

        String metadata,

        @NotBlank(message = "Signature is required") String signature) {
}
