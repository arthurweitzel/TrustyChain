package com.weitzel.trustychain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weitzel.trustychain.assistant.OllamaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class OllamaServiceTest {

    private OllamaService ollamaService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ollamaService = new OllamaService(WebClient.builder(), objectMapper);
        ReflectionTestUtils.setField(ollamaService, "ollamaBaseUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(ollamaService, "defaultModel", "llama3.2");
        ReflectionTestUtils.setField(ollamaService, "timeoutSeconds", 30);
    }

    @Test
    @DisplayName("Should create OllamaService with configuration")
    void shouldCreateOllamaServiceWithConfiguration() {
        assertNotNull(ollamaService);
    }

    @Test
    @DisplayName("Should generate response returns Mono")
    void shouldGenerateResponseReturnsMono() {
        var result = ollamaService.generateResponse("Test prompt");
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should check service availability returns Mono")
    void shouldCheckServiceAvailabilityReturnsMono() {
        var result = ollamaService.isServiceAvailable();
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should use custom model")
    void shouldUseCustomModel() {
        var result = ollamaService.generateResponse("Test", "custom-model");
        assertNotNull(result);
    }
}
