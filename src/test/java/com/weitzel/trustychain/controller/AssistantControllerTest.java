package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.AssistantRequest;
import com.weitzel.trustychain.model.DTO.AssistantResponse;
import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.service.AssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantControllerTest {

    @Mock
    private AssistantService assistantService;

    @InjectMocks
    private AssistantController assistantController;

    @Test
    void testKeysHelper() {
        AssistantRequest request = new AssistantRequest("How to generate keys?", "Java", "production");
        String expectedResponse = "Generate RSA keys using KeyPairGenerator...";
        
        when(assistantService.getKeyGenerationInstructions(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        ResponseEntity<AssistantResponse> response = assistantController.keysHelper(request, "Java");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody().answer());
    }

    @Test
    void testGenerateKeys() {
        String expectedResponse = "Key generation code for RSA 2048 on Java...";
        
        when(assistantService.generateKeyPair(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        ResponseEntity<AssistantResponse> response = assistantController.generateKeys("RSA", "2048", "Java");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody().answer());
    }

    @Test
    void testGenerateKeysWithBody() {
        KeyGenerationRequest request = new KeyGenerationRequest("RSA", "4096", "Python");
        String expectedResponse = "Key generation code for RSA 4096 on Python...";
        
        when(assistantService.generateKeyPair(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        ResponseEntity<AssistantResponse> response = assistantController.generateKeysWithBody(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody().answer());
    }
}