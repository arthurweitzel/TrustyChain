package com.weitzel.trustychain.assistant;

import com.weitzel.trustychain.assistant.dto.ChatRequest;
import com.weitzel.trustychain.assistant.dto.ChatResponse;
import com.weitzel.trustychain.assistant.dto.ErrorResponse;
import com.weitzel.trustychain.assistant.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/assistant")
@Tag(name = "AI Assistant", description = "AI-powered cryptographic assistant")
public class AssistantController {
    private final OllamaService ollamaService;

    public AssistantController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI crypto assistant")
    public Mono<ResponseEntity<?>> chatWithAssistant(@Valid @RequestBody ChatRequest request) {
        String prompt = String.format("""
                Answer the questions in english:

                Question: %s
                """, request.question());

        return ollamaService.generateResponse(prompt)
                .<ResponseEntity<?>>map(response -> ResponseEntity.ok(new ChatResponse(response)))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(
                        new ErrorResponse("AI service unavailable", e.getMessage()))));
    }

    @GetMapping("/health")
    @Operation(summary = "Check AI service health")
    public Mono<ResponseEntity<HealthResponse>> checkHealth() {
        return ollamaService.isServiceAvailable()
                .map(available -> ResponseEntity.ok(new HealthResponse("ollama", available)));
    }
}