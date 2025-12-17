package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.config.JwtAuthenticationFilter;
import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.service.JwtService;
import com.weitzel.trustychain.service.KeyGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KeyGenerationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class KeyGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeyGenerationService keyGenerationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void shouldGetKeyGenerationGuidanceWithQueryParams() throws Exception {
        // Mock service response
        when(keyGenerationService.getKeyGenerationGuidance(any()))
                .thenReturn(new com.weitzel.trustychain.model.DTO.KeyGenerationResponse(
                        "Test explanation",
                        "openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048",
                        new String[] { "Step 1", "Step 2" },
                        "Keep private key secure",
                        "Example output here"));

        mockMvc.perform(post("/api/key-generation/guidance")
                .param("platform", "openssl")
                .param("keySize", "2048")
                .param("useCase", "signing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation").value("Test explanation"))
                .andExpect(jsonPath("$.command")
                        .value("openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048"))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.securityNote").value("Keep private key secure"))
                .andExpect(jsonPath("$.exampleOutput").value("Example output here"));
    }

    @Test
    public void shouldGetKeyGenerationGuidanceWithBody() throws Exception {
        when(keyGenerationService.getKeyGenerationGuidance(any()))
                .thenReturn(new com.weitzel.trustychain.model.DTO.KeyGenerationResponse(
                        "Java key generation explanation",
                        "KeyPairGenerator keyGen = KeyPairGenerator.getInstance(\"RSA\");",
                        new String[] { "Import classes", "Initialize generator", "Generate keys" },
                        "Store private key securely",
                        "Java key pair generated"));

        KeyGenerationRequest request = new KeyGenerationRequest("java", "4096", "encryption");
        mockMvc.perform(post("/api/key-generation/guidance/detailed")
                .contentType("application/json")
                .content("{\"platform\":\"java\",\"keySize\":\"4096\",\"useCase\":\"encryption\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation").value("Java key generation explanation"))
                .andExpect(jsonPath("$.command")
                        .value("KeyPairGenerator keyGen = KeyPairGenerator.getInstance(\"RSA\");"));
    }

    @Test
    public void shouldGetKeyGenerationOptions() throws Exception {
        mockMvc.perform(get("/api/key-generation/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platforms").isArray())
                .andExpect(jsonPath("$.keySizes").isArray())
                .andExpect(jsonPath("$.useCases").isArray());
    }

    @Test
    public void shouldHandleMissingParameters() throws Exception {
        when(keyGenerationService.getKeyGenerationGuidance(any()))
                .thenReturn(new com.weitzel.trustychain.model.DTO.KeyGenerationResponse(
                        "Default guidance",
                        "Default command",
                        new String[] { "Default step" },
                        "Default security note",
                        "Default example"));

        mockMvc.perform(post("/api/key-generation/guidance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation").exists());
    }
}