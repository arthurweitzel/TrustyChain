package com.weitzel.trustychain.model.DTO;

public record AssistantRequest(
        String question,
        String clientLanguage,
        String environment
) {}


