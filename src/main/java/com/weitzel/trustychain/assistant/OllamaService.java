package com.weitzel.trustychain.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Netty imports for handling low-level socket timeouts
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring and WebFlux imports
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// Reactor (Mono/Flux) and Netty HTTP Client imports
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

// Standard Java imports
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OllamaService {
    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final WebClient webClient; // Final because we build it in the constructor
    private final ObjectMapper objectMapper;
    private final String defaultModel;

    public OllamaService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${ollama.model:llama3.2:1b}") String defaultModel // Default adjusted to your 1b model
    ) {
        this.objectMapper = objectMapper;
        this.defaultModel = defaultModel;

        // 1. Create the low-level Netty HTTP Client with extended timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000) // 30s connection timeout
                .responseTimeout(Duration.ofMinutes(10))              // 10m total response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.MINUTES))
                                .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.MINUTES)));

        // 2. Build the WebClient using the configured Netty connector
        this.webClient = webClientBuilder
                .baseUrl(ollamaBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public Mono<String> generateResponse(String prompt) {
        return generateResponse(prompt, defaultModel);
    }

    public Mono<String> generateResponse(String prompt, String model) {
        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "keep_alive", "10m");

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                // This timeout is an extra safety layer on the Mono itself
                .timeout(Duration.ofMinutes(10))
                .map(this::extractResponse)
                // IMPORTANT: Pass the exception 'error' to the logger to see the stack trace
                .doOnError(error -> log.error("Error calling Ollama API", error))
                .onErrorResume(error -> {
                    // Specific message for timeouts
                    if (error instanceof java.util.concurrent.TimeoutException) {
                        return Mono.just("O sistema demorou muito para responder (Timeout).");
                    }
                    return Mono.just("Desculpe, não foi possível gerar uma resposta no momento.");
                });
    }

    private String extractResponse(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            if (jsonNode.has("response")) {
                return jsonNode.get("response").asText();
            }
            return "Resposta vazia da IA.";
        } catch (Exception e) {
            log.error("Error parsing Ollama response", e);
            return "Erro ao processar resposta da IA.";
        }
    }

    public Mono<Boolean> isServiceAvailable() {
        return webClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5)) // Fast timeout for health checks
                .map(response -> true)
                .onErrorReturn(false);
    }
}