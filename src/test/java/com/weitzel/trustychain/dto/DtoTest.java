package com.weitzel.trustychain.dto;

import com.weitzel.trustychain.actor.dto.ActorRequest;
import com.weitzel.trustychain.assistant.dto.ChatRequest;
import com.weitzel.trustychain.assistant.dto.ChatResponse;
import com.weitzel.trustychain.assistant.dto.ErrorResponse;
import com.weitzel.trustychain.assistant.dto.HealthResponse;
import com.weitzel.trustychain.chain.dto.EventChainRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    @DisplayName("Should create ActorRequest")
    void shouldCreateActorRequest() {
        ActorRequest request = new ActorRequest("name", "user", "pass", "ROLE", "key");

        assertEquals("name", request.name());
        assertEquals("user", request.username());
        assertEquals("pass", request.password());
        assertEquals("ROLE", request.role());
        assertEquals("key", request.publicKey());
    }

    @Test
    @DisplayName("Should create ChatRequest")
    void shouldCreateChatRequest() {
        ChatRequest request = new ChatRequest("What is RSA?");
        assertEquals("What is RSA?", request.question());
    }

    @Test
    @DisplayName("Should create ChatResponse")
    void shouldCreateChatResponse() {
        ChatResponse response = new ChatResponse("RSA is an algorithm");
        assertEquals("RSA is an algorithm", response.response());
    }

    @Test
    @DisplayName("Should create ErrorResponse")
    void shouldCreateErrorResponse() {
        ErrorResponse response = new ErrorResponse("error", "message");
        assertEquals("error", response.error());
        assertEquals("message", response.message());
    }

    @Test
    @DisplayName("Should create HealthResponse")
    void shouldCreateHealthResponse() {
        HealthResponse response = new HealthResponse("ollama", true);
        assertEquals("ollama", response.service());
        assertTrue(response.available());
    }

    @Test
    @DisplayName("Should create EventChainRequest")
    void shouldCreateEventChainRequest() {
        EventChainRequest request = new EventChainRequest(
                "PROD-001", "Actor", "CREATE", "metadata", "signature");

        assertEquals("PROD-001", request.productCode());
        assertEquals("Actor", request.actor());
        assertEquals("CREATE", request.eventType());
        assertEquals("metadata", request.metadata());
        assertEquals("signature", request.signature());
    }

    @Test
    @DisplayName("Should handle null values in records")
    void shouldHandleNullValuesInRecords() {
        ChatRequest request = new ChatRequest(null);
        assertNull(request.question());
    }

    @Test
    @DisplayName("Should test equals and hashCode for ActorRequest")
    void shouldTestEqualsAndHashCodeForActorRequest() {
        ActorRequest r1 = new ActorRequest("n", "u", "p", "r", "k");
        ActorRequest r2 = new ActorRequest("n", "u", "p", "r", "k");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("Should test toString for records")
    void shouldTestToStringForRecords() {
        ChatRequest request = new ChatRequest("test");
        assertNotNull(request.toString());
        assertTrue(request.toString().contains("test"));
    }
}
