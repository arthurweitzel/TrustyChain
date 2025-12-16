package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.AssistantRequest;
import com.weitzel.trustychain.model.DTO.AssistantResponse;
import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.service.AssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/keys-helper")
    public ResponseEntity<AssistantResponse> keysHelper(@RequestBody AssistantRequest request,
                                                        @RequestParam(required = false) String platform) {
        String answer = assistantService.getKeyGenerationInstructions(
                request.question(),
                request.clientLanguage(),
                request.environment(),
                platform
        );
        return ResponseEntity.ok(new AssistantResponse(answer));
    }

    @PostMapping("/generate-keys")
    public ResponseEntity<AssistantResponse> generateKeys(
            @RequestParam(required = false, defaultValue = "RSA") String algorithm,
            @RequestParam(required = false, defaultValue = "2048") String keySize,
            @RequestParam(required = false) String platform) {
        String keyGenerationCode = assistantService.generateKeyPair(algorithm, keySize, platform);
        return ResponseEntity.ok(new AssistantResponse(keyGenerationCode));
    }

    @PostMapping("/generate-keys-body")
    public ResponseEntity<AssistantResponse> generateKeysWithBody(@RequestBody KeyGenerationRequest request) {
        String algorithm = request.algorithm() != null ? request.algorithm() : "RSA";
        String keySize = request.keySize() != null ? request.keySize() : "2048";
        String platform = request.platform();

        String keyGenerationCode = assistantService.generateKeyPair(algorithm, keySize, platform);
        return ResponseEntity.ok(new AssistantResponse(keyGenerationCode));
    }
}


