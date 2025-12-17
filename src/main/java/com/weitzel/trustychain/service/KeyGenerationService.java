package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.DTO.KeyGenerationRequest;
import com.weitzel.trustychain.model.DTO.KeyGenerationResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class KeyGenerationService {

    private final OllamaService ollamaService;

    public KeyGenerationService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public KeyGenerationResponse getKeyGenerationGuidance(KeyGenerationRequest request) {
        // Always try Ollama first, fallback to static if it fails
        try {
            System.out.println(" Tentando gerar resposta com Ollama...");
            String prompt = buildPrompt(request);
            String aiResponse = ollamaService.generateResponse(prompt)
                .timeout(Duration.ofSeconds(300))
                .block();
            System.out.println(" Resposta Ollama recebida: " + (aiResponse != null ? aiResponse.substring(0, Math.min(100, aiResponse.length())) : "null"));
            
            if (aiResponse != null && !aiResponse.contains("Desculpe, não foi possível gerar")) {
                System.out.println(" Usando resposta da IA!");
                return parseAIResponse(aiResponse, request);
            }
        } catch (Exception e) {
            System.out.println("Erro ao chamar Ollama: " + e.getMessage());
        }
        
        System.out.println(" Usando resposta estática (fallback)");
        return buildStaticResponse(request);
    }

    /**
     * Asynchronous version that always tries to use Ollama AI.
     */
    public Mono<KeyGenerationResponse> getKeyGenerationGuidanceAsync(KeyGenerationRequest request) {
        String prompt = buildPrompt(request);
        
        return ollamaService.generateResponse(prompt)
            .<KeyGenerationResponse>map(aiResponse -> parseAIResponse(aiResponse, request))
            .onErrorReturn(buildStaticResponse(request));
    }

    /**
     * Builds a comprehensive prompt for Ollama AI.
     */
    private String buildPrompt(KeyGenerationRequest request) {
        String platform = request.platform() != null ? request.platform() : "openssl";
        String keySize = request.keySize() != null ? request.keySize() : "2048";
        String useCase = request.useCase() != null ? request.useCase() : "signing";

        return """
            Você é um especialista em segurança e criptografia. Forneça instruções detalhadas e seguras 
            para gerar chaves RSA
            
            Responda em português e inclua:
            1. Explicação clara do processo
            2. Comandos exatos para executar
            3. Passos detalhados
            4. Notas de segurança importantes
            5. Exemplo de saída esperada
            
            Seja específico e inclua apenas informações relevantes e seguras.
            """;
    }

    /**
     * Parses AI response and formats it into KeyGenerationResponse.
     */
    private KeyGenerationResponse parseAIResponse(String aiResponse, KeyGenerationRequest request) {
        String platform = request.platform() != null ? request.platform().toLowerCase() : "openssl";
        String keySize = request.keySize() != null ? request.keySize() : "2048";
        
        String command = extractCommand(aiResponse, platform, keySize);
        String[] steps = extractSteps(aiResponse);
        String securityNote = extractSecurityNote(aiResponse, platform);
        String exampleOutput = extractExampleOutput(aiResponse, platform);
        
        return new KeyGenerationResponse(
            aiResponse, // Use full AI response as explanation
            command,
            steps,
            securityNote,
            exampleOutput
        );
    }

    private String extractCommand(String aiResponse, String platform, String keySize) {
        // Fallback to static command generation if AI doesn't provide clear commands
        switch (platform) {
            case "openssl":
                return String.format("openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:%s", keySize);
            case "java":
                return String.format(
                    "KeyPairGenerator keyGen = KeyPairGenerator.getInstance(\"RSA\");\n" +
                    "keyGen.initialize(%s);\n" +
                    "KeyPair keyPair = keyGen.generateKeyPair();", keySize);
            default:
                return "Comando específico para " + platform + " será fornecido baseado nos requisitos";
        }
    }

    private String[] extractSteps(String aiResponse) {
        // Simple extraction - split by lines that look like steps
        String[] lines = aiResponse.split("\n");
        java.util.List<String> steps = new java.util.ArrayList<>();
        
        for (String line : lines) {
            if (line.trim().matches("\\d+\\..*") || line.trim().toLowerCase().contains("passo")) {
                steps.add(line.trim());
            }
        }
        
        if (steps.isEmpty()) {
            // Fallback to static steps
            return new String[]{
                "1. Verifique se a plataforma está instalada",
                "2. Execute o comando fornecido",
                "3. Proteja a chave privada",
                "4. Extraia a chave pública se necessário"
            };
        }
        
        return steps.toArray(new String[0]);
    }

    private String extractSecurityNote(String aiResponse, String platform) {
        if (aiResponse.toLowerCase().contains("segurança") || aiResponse.toLowerCase().contains("security")) {
            return aiResponse;
        }
        
        return String.format(
            "Para geração de chaves RSA em %s: Use sempre chaves de no mínimo 2048 bits (3072+ recomendado para segurança a longo prazo). " +
            "Nunca compartilhe chaves privadas, use armazenamento seguro e considere um Módulo de Segurança de Hardware (HSM) " +
            "para ambientes de produção. Faça backup das chaves de forma segura e implemente políticas de rotação adequadas.",
            platform
        );
    }

    private String extractExampleOutput(String aiResponse, String platform) {
        switch (platform) {
            case "openssl":
                return "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKiwggSiAgEAAoIBAQC...\n-----END PRIVATE KEY-----";
            case "java":
                return "PublicKey: Sun RSA public key, 2048 bits\nmodulus: 1234567890...\npublic exponent: 65537";
            default:
                return "A saída dependerá da plataforma escolhida e das ferramentas utilizadas.";
        }
    }

    /**
     * Builds comprehensive response for RSA key generation guidance (fallback method).
     */
    private KeyGenerationResponse buildStaticResponse(KeyGenerationRequest request) {
        String platform = request.platform() != null ? request.platform().toLowerCase() : "openssl";
        String keySize = request.keySize() != null ? request.keySize() : "2048";
        String useCase = request.useCase() != null ? request.useCase() : "signing";

        return new KeyGenerationResponse(
            generateExplanation(platform, keySize, useCase),
            generateCommand(platform, keySize),
            generateSteps(platform),
            generateSecurityNote(platform),
            generateExampleOutput(platform)
        );
    }

    private String generateExplanation(String platform, String keySize, String useCase) {
        return String.format(
            "Este guia irá ajudá-lo a gerar um par de chaves RSA %s-bit seguro usando %s. " +
            "A chave será adequada para %s e segue as melhores práticas de segurança atuais.",
            keySize, platform, useCase
        );
    }

    private String generateCommand(String platform, String keySize) {
        switch (platform) {
            case "openssl":
                return String.format("openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:%s", keySize);
            case "java":
                return String.format(
                    "KeyPairGenerator keyGen = KeyPairGenerator.getInstance(\"RSA\");\n" +
                    "keyGen.initialize(%s);\n" +
                    "KeyPair keyPair = keyGen.generateKeyPair();", keySize);
            case "python":
                return String.format(
                    "from cryptography.hazmat.primitives.asymmetric import rsa\n" +
                    "from cryptography.hazmat.primitives import serialization\n\n" +
                    "private_key = rsa.generate_private_key(\n" +
                    "    public_exponent=65537,\n" +
                    "    key_size=%s\n" +
                    ")", keySize);
            case "javascript":
                return String.format(
                    "const crypto = require('crypto');\n" +
                    "const { privateKey, publicKey } = crypto.generateKeyPairSync('rsa', {\n" +
                    "  modulusLength: %s,\n" +
                    "  publicKeyEncoding: { type: 'spki', format: 'pem' },\n" +
                    "  privateKeyEncoding: { type: 'pkcs8', format: 'pem' }\n" +
                    "});", keySize);
            default:
                return "Comando específico para a plataforma será fornecido com base nos requisitos";
        }
    }

    private String[] generateSteps(String platform) {
        switch (platform) {
            case "openssl":
                return new String[]{
                    "1. Instale OpenSSL se ainda não estiver instalado",
                    "2. Abra seu terminal ou prompt de comando",
                    "3. Execute o comando OpenSSL com o tamanho de chave desejado",
                    "4. Proteja o arquivo de chave privada com permissões apropriadas",
                    "5. Extraia a chave pública se necessário usando: openssl rsa -in private_key.pem -pubout -out public_key.pem"
                };
            case "java":
                return new String[]{
                    "1. Adicione Java Cryptography Extension (JCE) ao seu projeto",
                    "2. Crie uma instância KeyPairGenerator para RSA",
                    "3. Inicialize com o tamanho de chave desejado (2048+ recomendado)",
                    "4. Gere o par de chaves",
                    "5. Armazene as chaves com segurança usando codificação apropriada"
                };
            case "python":
                return new String[]{
                    "1. Instale a biblioteca cryptography: pip install cryptography",
                    "2. Importe os módulos necessários de cryptography",
                    "3. Use rsa.generate_private_key() com o tamanho de chave desejado",
                    "4. Serialize as chaves para formato PEM para armazenamento",
                    "5. Armazene a chave privada com segurança e compartilhe a chave pública"
                };
            case "javascript":
                return new String[]{
                    "1. Instale Node.js se ainda não estiver instalado",
                    "2. Use o módulo crypto nativo do Node.js",
                    "3. Chame generateKeyPairSync com parâmetros RSA",
                    "4. Escolha formato de codificação de chave apropriado",
                    "5. Armazene as chaves com segurança, especialmente a chave privada"
                };
            default:
                return new String[]{
                    "1. Escolha uma biblioteca criptográfica compatível",
                    "2. Siga as diretrizes de segurança específicas da plataforma",
                    "3. Use chaves de tamanho mínimo 2048-bit para segurança",
                    "4. Armazene chaves privadas com proteção adequada",
                    "5. Teste as chaves antes do uso em produção"
                };
        }
    }

    private String generateSecurityNote(String platform) {
        return String.format(
            "Para geração de chaves RSA em %s: Use sempre chaves de mínimo 2048 bits (3072+ recomendado para segurança a longo prazo). " +
            "Nunca compartilhe chaves privadas, use armazenamento seguro e considere um Módulo de Segurança de Hardware (HSM) " +
            "para ambientes de produção. Faça backup das chaves com segurança e implemente políticas de rotação de chaves adequadas.",
            platform
        );
    }

    private String generateExampleOutput(String platform) {
        switch (platform) {
            case "openssl":
                return "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKiwggSiAgEAAoIBAQC...\n-----END PRIVATE KEY-----";
            case "java":
                return "PublicKey: Sun RSA public key, 2048 bits\n" +
                       "modulus: 1234567890...\n" +
                       "public exponent: 65537";
            case "python":
                return "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKiwggSiAgEAAoIBAQC...\n-----END PRIVATE KEY-----";
            case "javascript":
                return "Public Key:\n-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----\n" +
                       "Private Key:\n-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBK...\n-----END PRIVATE KEY-----";
            default:
                return "A saída de exemplo dependerá da plataforma e ferramentas escolhidas.";
        }
    }
}