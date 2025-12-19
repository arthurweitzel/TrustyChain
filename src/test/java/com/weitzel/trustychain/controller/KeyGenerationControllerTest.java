package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.assistant.KeyGenerationController;
import com.weitzel.trustychain.auth.JwtAuthenticationFilter;
import com.weitzel.trustychain.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KeyGenerationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class KeyGenerationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Test
        public void shouldGenerateKeyPair() throws Exception {
                mockMvc.perform(get("/api/key-generation"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.publicKey").exists())
                                .andExpect(jsonPath("$.privateKey").exists())
                                .andExpect(jsonPath("$.publicKey")
                                                .value(org.hamcrest.Matchers.containsString("BEGIN PUBLIC KEY")))
                                .andExpect(jsonPath("$.privateKey")
                                                .value(org.hamcrest.Matchers.containsString("BEGIN PRIVATE KEY")));
        }

        @Test
        public void shouldSignDataWithPrivateKey() throws Exception {
                // First generate a key pair
                String keyPairResponse = mockMvc.perform(get("/api/key-generation"))
                                .andReturn().getResponse().getContentAsString();

                // Extract private key (simple JSON parsing)
                String privateKey = keyPairResponse
                                .replaceAll(".*\"privateKey\":\"", "")
                                .replaceAll("\".*", "")
                                .replace("\\n", "\n");

                String signRequest = String.format(
                                "{\"data\":\"test data to sign\",\"privateKey\":\"%s\"}",
                                privateKey.replace("\n", "\\n"));

                mockMvc.perform(post("/api/key-generation/sign")
                                .contentType("application/json")
                                .content(signRequest))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.signature").exists());
        }
}