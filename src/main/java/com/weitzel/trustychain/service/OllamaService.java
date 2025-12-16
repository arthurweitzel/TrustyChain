package com.weitzel.trustychain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class OllamaService {

    private final WebClient webClient;
    private final String ollamaBaseUrl;
    private final String ollamaModel;
    private final ObjectMapper objectMapper;

    public OllamaService(WebClient.Builder webClientBuilder,
                         @Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl,
                         @Value("${spring.ai.ollama.chat.model}") String ollamaModel,
                         ObjectMapper objectMapper) {
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaModel = ollamaModel;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(ollamaBaseUrl).build();
    }

    public String getDynamicKeyGenerationInstructions(String question, String clientLanguage, String environment, String platform) {
        String prompt = String.format("""
                You are a cryptographic assistant specialized in asymmetric key generation using DeepSeek model capabilities.
                The user needs to generate cryptographic keys for blockchain-style signatures with the following context:
                Question: %s
                Client Language: %s
                Environment: %s
                Preferred Platform: %s

                Generate comprehensive instructions for creating a secure key pair (RSA 2048/4096-bit or ECDSA P-256/P-384) on the specified platform.
                
                CRITICAL SECURITY REQUIREMENTS:
                - The private key must NEVER leave the client application under any circumstances
                - Only the public key should be shared or registered with the system
                - Private key must be stored in a secure keystore or hardware security module
                - Implement proper key rotation and backup strategies
                
                SIGNING FORMAT:
                The signature payload format is: 'previousHash | actor | productCode | eventType | metadata'
                
                Provide:
                1. Step-by-step key generation commands/code for %s
                2. Public key extraction and format (PEM/DER/Base64)
                3. Secure private key storage recommendations
                4. Sample signing implementation for the specified payload format
                5. Verification process using the public key
                6. Error handling and security best practices
                
                Ensure all examples are production-ready and follow current cryptographic standards.
                """, question, clientLanguage, environment, platform, platform);

        try {
            // Construct the request body for Ollama chat API
            ObjectNode requestBodyJson = objectMapper.createObjectNode();
            requestBodyJson.put("model", ollamaModel);
            requestBodyJson.put("stream", false);
            ArrayNode messagesArray = requestBodyJson.putArray("messages");
            ObjectNode messageObject = messagesArray.addObject();
            messageObject.put("role", "user");
            messageObject.put("content", prompt);

            System.out.println("Ollama Request Body: " + requestBodyJson.toPrettyString());

            JsonNode responseBody = webClient.post()
                    .uri("/api/chat") // Assuming Ollama chat endpoint is /api/chat
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBodyJson)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // Parse the JSON response
            return responseBody.path("message").path("content").asText();
        } catch (Exception e) {
            System.err.println("Error calling Ollama API: " + e.getMessage());
            return "Error: Could not retrieve dynamic instructions from Ollama. Please check the Ollama service status and configuration.";
        }
    }

    public String generateKeyPairWithDeepSeek(String algorithm, String keySize, String platform) {
        String prompt = String.format("""
                As a DeepSeek cryptographic expert, generate a complete key pair solution for:
                Algorithm: %s
                Key Size: %s
                Platform: %s
                
                CRITICAL: This is for cryptographic signing in a blockchain system.
                
                Provide:
                1. Complete key generation code that produces BOTH private and public keys
                2. Private key MUST stay on client side - never transmit
                3. Public key in the exact format needed for registration (PEM/Base64)
                4. Example of signing the payload: 'previousHash|actor|productCode|eventType|metadata'
                5. Complete verification code using the public key
                6. Production-ready error handling and key storage
                
                The private key should be generated client-side and NEVER leave the client application.
                Only provide the public key format for server registration.
                
                Ensure the code follows current cryptographic best practices and is secure against common attacks.
                """, algorithm, keySize, platform);

        try {
            ObjectNode requestBodyJson = objectMapper.createObjectNode();
            requestBodyJson.put("model", ollamaModel);
            requestBodyJson.put("stream", false);
            requestBodyJson.put("temperature", 0.1); // Lower temperature for more consistent cryptographic output
            
            ArrayNode messagesArray = requestBodyJson.putArray("messages");
            ObjectNode messageObject = messagesArray.addObject();
            messageObject.put("role", "user");
            messageObject.put("content", prompt);

            JsonNode responseBody = webClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBodyJson)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(45))
                    .block();

            return responseBody.path("message").path("content").asText();
        } catch (Exception e) {
            System.err.println("Error calling DeepSeek for key generation: " + e.getMessage());
            throw new RuntimeException("Failed to generate cryptographic keys using DeepSeek model", e);
        }
    }
}