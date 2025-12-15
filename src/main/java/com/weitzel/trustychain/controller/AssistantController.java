package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.AssistantRequest;
import com.weitzel.trustychain.model.DTO.AssistantResponse;
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
    public ResponseEntity<AssistantResponse> keysHelper(@RequestBody AssistantRequest request) {
        String answer = assistantService.generateKeyGuidance(
                request.question(),
                request.clientLanguage(),
                request.environment()
        );
        return ResponseEntity.ok(new AssistantResponse(answer));
    }
}


