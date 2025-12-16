package com.weitzel.trustychain.service;

import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private final OllamaService ollamaService;

    public AssistantService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public String getKeyGenerationInstructions(String question, String clientLanguage, String environment, String platform) {
        if (platform == null || platform.trim().isEmpty()) {
            return "Please specify your preferred platform for key generation (e.g., 'Java', 'Python', 'Node.js', 'OpenSSL', 'CLI'). This will help me provide tailored instructions.";
        }

        return ollamaService.getDynamicKeyGenerationInstructions(question, clientLanguage, environment, platform);
    }

    public String generateKeyPair(String algorithm, String keySize, String platform) {
        if (algorithm == null || algorithm.trim().isEmpty()) {
            algorithm = "RSA"; // Default to RSA
        }
        if (keySize == null || keySize.trim().isEmpty()) {
            keySize = "2048"; // Default to 2048-bit for RSA
        }
        if (platform == null || platform.trim().isEmpty()) {
            return "Please specify your preferred platform (e.g., 'Java', 'Python', 'Node.js', 'OpenSSL').";
        }

        return ollamaService.generateKeyPairWithDeepSeek(algorithm, keySize, platform);
    }
}

