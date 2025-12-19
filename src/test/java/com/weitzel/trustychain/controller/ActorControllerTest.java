package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorController;
import com.weitzel.trustychain.actor.ActorService;
import com.weitzel.trustychain.auth.JwtAuthenticationFilter;
import com.weitzel.trustychain.auth.JwtService;
import com.weitzel.trustychain.chain.ProductChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActorService actorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Actor testActor;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testActor = new Actor();
        testActor.setId(testId);
        testActor.setName("Test Actor");
        testActor.setUsername("testuser");
        testActor.setPassword("encoded");
        testActor.setRole("PRODUCER");
        testActor.setPublicKey("-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----");
    }

    @Test
    @DisplayName("Should register actor")
    void shouldRegisterActor() throws Exception {
        when(actorService.registerActor(any(), any(), any(), any(), any()))
                .thenReturn(testActor);

        String requestBody = """
                {
                    "name": "Test Actor",
                    "username": "testuser",
                    "password": "password",
                    "role": "PRODUCER",
                    "publicKey": "-----BEGIN PUBLIC KEY-----\\ntest\\n-----END PUBLIC KEY-----"
                }
                """;

        mockMvc.perform(post("/api/actors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Actor"));
    }

    @Test
    @DisplayName("Should get all actors")
    void shouldGetAllActors() throws Exception {
        when(actorService.findAllActors()).thenReturn(List.of(testActor));

        mockMvc.perform(get("/api/actors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Actor"));
    }

    @Test
    @DisplayName("Should get actor by ID")
    void shouldGetActorById() throws Exception {
        when(actorService.findActorById(testId)).thenReturn(testActor);

        mockMvc.perform(get("/api/actors/" + testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Actor"));
    }

    @Test
    @DisplayName("Should get signatures by actor")
    void shouldGetSignaturesByActor() throws Exception {
        ProductChain event = new ProductChain();
        event.setProductCode("PROD-001");
        event.setCurrentHash("hash123");
        event.setCreatedAt(java.time.LocalDateTime.now());
        when(actorService.getEventsByActor(testId)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/actors/" + testId + "/signatures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product").value("PROD-001"));
    }

    @Test
    @DisplayName("Should update actor")
    void shouldUpdateActor() throws Exception {
        testActor.setName("Updated Name");
        when(actorService.updateActor(eq(testId), any())).thenReturn(testActor);

        String requestBody = """
                {
                    "name": "Updated Name",
                    "username": "testuser",
                    "password": "password",
                    "role": "PRODUCER",
                    "publicKey": "key"
                }
                """;

        mockMvc.perform(put("/api/actors/" + testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("Should delete actor")
    void shouldDeleteActor() throws Exception {
        doNothing().when(actorService).deleteActor(testId);

        mockMvc.perform(delete("/api/actors/" + testId))
                .andExpect(status().isNoContent());

        verify(actorService).deleteActor(testId);
    }
}
