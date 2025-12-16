package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.entity.Actor;
import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.repository.ActorRepository;
import com.weitzel.trustychain.repository.ProductChainRepository;
import com.weitzel.trustychain.util.helper.ChainHelper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

@Service
public class ProductChainService {
    private final ProductChainRepository productChainRepository;
    private final ActorRepository actorRepository;

    public ProductChainService(ProductChainRepository productChainRepository, ActorRepository actorRepository) {
        this.productChainRepository = productChainRepository;
        this.actorRepository = actorRepository;
    }

    // transactional secures that if the change couldnt be accepted it will be rollbacked
    @Transactional
    public ProductChain registerEvent(String actorName, String productCode, String eventType, String metadata, String signatureBase64) {
        Actor actor = actorRepository.findByName(actorName)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        String lastHash = productChainRepository.findTopByProductCodeOrderByCreatedAtDesc(productCode)
                .map(ProductChain::getCurrentHash)
                .orElse(null);

        // this BEGIN string could be anything, since its only "thash" to be used to generate the hash itself
        String data = (lastHash == null ? "BEGIN" : lastHash)
                + actorName
                + productCode
                + eventType
                + metadata;

        PublicKey publicKey = ChainHelper.loadPublicKeyFromPem(actor.getPublicKey());
        boolean validSignature = ChainHelper.verifySignature(data.getBytes(StandardCharsets.UTF_8), signatureBase64, publicKey);
        if (!validSignature) {
            throw new RuntimeException("Invalid signature for event");
        }

        ProductChain productChain = new ProductChain(actorName, productCode, eventType, metadata, lastHash, signatureBase64, actor.getPublicKey());
        return productChainRepository.save(productChain);
    }

    public boolean verifyChainIntegrity(String productCode) {
        List<ProductChain> events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(productCode);

        if (events.isEmpty()) {
            return false;
        }

        String previousHash = null;

        for (ProductChain event : events) {
            // previous hash stored in the row must match the calculated previous link
            if (!Objects.equals(event.getPreviousHash(), previousHash)) {
                return false;
            }

            try {
                String data = (previousHash == null ? "BEGIN" : previousHash)
                        + event.getActor()
                        + event.getProductCode()
                        + event.getEventType()
                        + event.getMetadata();

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
                String expectedHash = com.weitzel.trustychain.util.helper.ChainHelper.bytesToHex(hash);

                if (!expectedHash.equals(event.getCurrentHash())) {
                    return false;
                }

                PublicKey publicKey = ChainHelper.loadPublicKeyFromPem(event.getPublicKeySnapshot());
                boolean validSignature = ChainHelper.verifySignature(data.getBytes(StandardCharsets.UTF_8), event.getSignature(), publicKey);
                if (!validSignature) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }

            previousHash = event.getCurrentHash();
        }

        return true;
    }
}
