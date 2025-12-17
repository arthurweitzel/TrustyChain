package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.model.DTO.KeyGenerationResponse;
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

/**
 * Controller for RSA key generation guidance endpoint.
 * Provides AI-powered assistance for secure RSA key generation.
 */
@RestController
@RequestMapping("/api/key-generation")
@Tag(name = "Key Generation", description = "AI-powered RSA key generation guidance")
public class KeyGenerationController {

    private final KeyGenerationService keyGenerationService;

    public KeyGenerationController(KeyGenerationService keyGenerationService) {
        this.keyGenerationService = keyGenerationService;
    }

    /**
     * Get AI-powered guidance for generating RSA keys.
     */
    @PostMapping("/guidance")
    @Operation(
        summary = "Get RSA key generation guidance",
        description = "Provides step-by-step instructions for generating RSA keys on different platforms with security best practices"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated key generation guidance",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = KeyGenerationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or AI service unavailable"
        )
    })
    public ResponseEntity<KeyGenerationResponse> getKeyGenerationGuidance(
            @Parameter(description = "Platform for key generation (openssl, java, python, javascript)")
            @RequestParam(required = false) String platform,
            
            @Parameter(description = "Key size in bits (2048, 3072, 4096)")
            @RequestParam(required = false) String keySize,
            
            @Parameter(description = "Intended use case (signing, encryption, both)")
            @RequestParam(required = false) String useCase) {
        
        KeyGenerationRequest request = new KeyGenerationRequest(platform, keySize, useCase);
        KeyGenerationResponse response = keyGenerationService.getKeyGenerationGuidance(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get guidance with request body for complex scenarios.
     */
    @PostMapping("/guidance/detailed")
    @Operation(
        summary = "Get detailed RSA key generation guidance",
        description = "Provides comprehensive guidance for complex key generation scenarios"
    )
    public ResponseEntity<KeyGenerationResponse> getDetailedKeyGenerationGuidance(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Key generation parameters",
                required = true
            )
            @RequestBody KeyGenerationRequest request) {
        
        KeyGenerationResponse response = keyGenerationService.getKeyGenerationGuidance(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get supported platforms and key sizes.
     */
    @GetMapping("/options")
    @Operation(
        summary = "Get supported key generation options",
        description = "Returns supported platforms, key sizes, and use cases"
    )
    public ResponseEntity<KeyGenerationOptions> getKeyGenerationOptions() {
        KeyGenerationOptions options = new KeyGenerationOptions(
            new String[]{"openssl", "java", "python", "javascript"},
            new String[]{"2048", "3072", "4096"},
            new String[]{"signing", "encryption", "both"}
        );
        return ResponseEntity.ok(options);
    }

    /**
     * Record for key generation options.
     */
    public record KeyGenerationOptions(
            String[] platforms,
            String[] keySizes,
            String[] useCases
    ) {}
}