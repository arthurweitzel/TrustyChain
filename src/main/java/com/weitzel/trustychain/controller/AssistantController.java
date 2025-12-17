package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.service.KeyGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for AI-powered assistant endpoints.
 * Provides dynamic AI responses using Ollama.
 */
@RestController
@RequestMapping("/api/assistant")
@Tag(name = "AI Assistant", description = "AI-powered cryptographic assistant")
public class AssistantController {

    private final KeyGenerationService keyGenerationService;

    public AssistantController(KeyGenerationService keyGenerationService) {
        this.keyGenerationService = keyGenerationService;
    }

    /**
     * Dynamic AI-powered RSA key generation helper.
     */
    @PostMapping("/keys-helper")
    @Operation(
        summary = "Get AI-powered RSA key generation help",
        description = "Provides dynamic, AI-generated guidance for RSA key generation using Ollama models"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated AI response",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"explanation\":\"Explicação detalhada...\",\"command\":\"comando...\",\"steps\":[...]}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "AI service unavailable"
        )
    })
    public Mono<ResponseEntity<Object>> getKeysHelper(
            @Parameter(description = "Platform for key generation (openssl, java, python, javascript)")
            @RequestParam(required = false) String platform,
            
            @Parameter(description = "Key size in bits (2048, 3072, 4096)")
            @RequestParam(required = false) String keySize,
            
            @Parameter(description = "Intended use case (signing, encryption, both)")
            @RequestParam(required = false) String useCase,
            
            @Parameter(description = "Use async AI generation")
            @RequestParam(defaultValue = "false") boolean async) {

        if (async) {
            return keyGenerationService.getKeyGenerationGuidanceAsync(
                new com.weitzel.trustychain.model.DTO.KeyGenerationRequest(platform, keySize, useCase)
            ).map(ResponseEntity::ok);
        } else {
            try {
                var response = keyGenerationService.getKeyGenerationGuidance(
                    new com.weitzel.trustychain.model.DTO.KeyGenerationRequest(platform, keySize, useCase)
                );
                return Mono.just(ResponseEntity.ok(response));
            } catch (Exception e) {
                return Mono.just(ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to generate AI response", "message", e.getMessage())
                ));
            }
        }
    }

    /**
     * Generic AI chat endpoint for crypto questions.
     */
    @PostMapping("/chat")
    @Operation(
        summary = "Chat with AI crypto assistant",
        description = "Ask general questions about cryptography, security, and blockchain"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated AI response"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    public Mono<ResponseEntity<Map<String, String>>> chatWithAssistant(
            @Parameter(description = "Your question about cryptography")
            @RequestBody Map<String, String> request) {
        
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(
                Map.of("error", "Question is required")
            ));
        }

        String prompt = String.format("""
            Você é um especialista em criptografia, blockchain e segurança. Responda à seguinte pergunta 
            de forma clara, precisa e em português:
            
            Pergunta: %s
            
            Forneça informações precisas, atualizadas e relevantes sobre o tema.
            Se a pergunta envolver recomendações de segurança, inclua as melhores práticas atuais.
            """, question);

        // This would need OllamaService injected directly for this endpoint
        // For now, return a fallback response
        return Mono.just(ResponseEntity.ok(Map.of(
            "response", "Serviço de chat AI em desenvolvimento. Use o endpoint /keys-helper para assistência com geração de chaves."
        )));
    }
}