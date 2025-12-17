package com.weitzel.trustychain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Service for interacting with Ollama API to generate dynamic AI responses.
 */
@Service
public class OllamaService {
    
    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);
    
    private WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${ollama.model:llama3.2}")
    private String defaultModel;
    
    @Value("${ollama.timeout:30}")
    private int timeoutSeconds;
    
    public OllamaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
        }
        return webClient;
    }
    
    /**
     * Generates a response from Ollama for the given prompt.
     */
    public Mono<String> generateResponse(String prompt) {
        return generateResponse(prompt, defaultModel);
    }
    
    /**
     * Generates a response from Ollama for the given prompt using specified model.
     */
    public Mono<String> generateResponse(String prompt, String model) {
        Map<String, Object> request = Map.of(
            "model", model,
            "prompt", prompt,
            "stream", false
        );
        
        return getWebClient().post()
            .uri("/api/generate")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .map(this::extractResponse)
            .doOnError(error -> log.error("Error calling Ollama API: {}", error.getMessage()))
            .onErrorReturn("Desculpe, não foi possível gerar uma resposta no momento. Tente novamente mais tarde.");
    }
    
    /**
     * Extracts the actual response content from Ollama's JSON response.
     */
    private String extractResponse(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            return jsonNode.get("response").asText();
        } catch (Exception e) {
            log.error("Error parsing Ollama response: {}", e.getMessage());
            return "Erro ao processar resposta da IA.";
        }
    }
    
    /**
     * Checks if Ollama service is available.
     */
    public Mono<Boolean> isServiceAvailable() {
        return getWebClient().get()
            .uri("/api/tags")
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(5))
            .map(response -> true)
            .onErrorReturn(false);
    }
}