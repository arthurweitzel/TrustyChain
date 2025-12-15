package com.weitzel.trustychain.service;

import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    public String generateKeyGuidance(String question, String clientLanguage, String environment) {
        String lang = clientLanguage == null ? "desconhecida" : clientLanguage;
        String env = environment == null ? "desconhecido" : environment;

        StringBuilder sb = new StringBuilder();
        sb.append("### Checklist de arquitetura para geração de chaves pelo cliente\n");
        sb.append("- Chave privada permanece **somente** no cliente? -> SIM (servidor recebe apenas a chave pública).\n");
        sb.append("- Endpoint de cadastro de ator aceita apenas `publicKey`? -> SIM (`POST /api/actors`).\n");
        sb.append("- O servidor nunca devolve nem armazena chave privada? -> SIM (modelo atual da sua API).\n");
        sb.append("- Payload assinado é sempre: `previousHash | actor | productCode | eventType | metadata`? -> SIM (conforme `ProductChainService`).\n\n");

        sb.append("### Como gerar chave e assinar no cliente (visão geral)\n");
        sb.append("Contexto detectado: linguagem = ").append(lang).append(", ambiente = ").append(env).append(".\n\n");

        sb.append("1. Gere um par de chaves assimétricas (RSA 2048 ou ECDSA P-256).\n");
        sb.append("2. Guarde a **chave privada** em local seguro (nunca envie para o TrustyChain).\n");
        sb.append("3. Envie apenas a **chave pública** em formato PEM para `POST /api/actors`.\n");
        sb.append("4. Para cada evento:\n");
        sb.append("   - Monte a string exata: `previousHash + actor + productCode + eventType + metadata`.\n");
        sb.append("   - Calcule o hash SHA-256 desse payload.\n");
        sb.append("   - Assine o hash com sua chave privada (ex.: algoritmo `SHA256withRSA`).\n");
        sb.append("   - Envie a assinatura em base64 no campo `signature` de `POST /api/product-chain/event`.\n\n");

        sb.append("### Próximos passos sugeridos\n");
        sb.append("- Escolha sua stack (Node, Java, Python, CLI) e gere as chaves lá.\n");
        sb.append("- Crie funções utilitárias no seu sistema para: (a) gerar/rotacionar chaves, (b) assinar eventos, (c) enviar para o TrustyChain.\n");
        sb.append("- Use ambientes de teste (dev/sandbox) antes de usar chaves de produção.\n\n");

        sb.append("Pergunta original do usuário: ").append(question == null ? "(vazia)" : question).append("\n");

        return sb.toString();
    }
}


