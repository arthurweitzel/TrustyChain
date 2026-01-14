package com.weitzel.trustychain.chain;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorRepository;
import com.weitzel.trustychain.common.exception.Exceptions;
import com.weitzel.trustychain.common.service.CryptoService;
import com.weitzel.trustychain.common.service.HashService;
import com.weitzel.trustychain.common.service.SignedTimestamp;
import com.weitzel.trustychain.common.service.TimestampService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

@Service
public class ProductChainService {
    private final ProductChainRepository productChainRepository;
    private final ActorRepository actorRepository;
    private final HashService hashService;
    private final CryptoService cryptoService;
    private final TimestampService timestampService;

    public ProductChainService(ProductChainRepository productChainRepository,
            ActorRepository actorRepository,
            HashService hashService,
            CryptoService cryptoService,
            TimestampService timestampService) {
        this.productChainRepository = productChainRepository;
        this.actorRepository = actorRepository;
        this.hashService = hashService;
        this.cryptoService = cryptoService;
        this.timestampService = timestampService;
    }

    @Transactional
    public ProductChain registerEvent(String actorName, String productCode, String eventType,
            String metadata, String signatureBase64) {
        Actor actor = actorRepository.findByName(actorName)
                .orElseThrow(() -> new Exceptions.ActorNotFoundException("Actor not found: " + actorName));

        String lastHash = productChainRepository.findTopByProductCodeOrderByCreatedAtDesc(productCode)
                .map(ProductChain::getCurrentHash)
                .orElse(null);

        String data = (lastHash == null ? "BEGIN" : lastHash)
                + actorName
                + productCode
                + eventType
                + metadata;

        PublicKey publicKey = cryptoService.loadPublicKeyFromPem(actor.getPublicKey());
        boolean validSignature = cryptoService.verifySignature(
                data.getBytes(StandardCharsets.UTF_8), signatureBase64, publicKey);

        if (!validSignature) {
            throw new Exceptions.InvalidSignatureException(
                    "Invalid signature for event on product: " + productCode);
        }

        String currentHash = hashService.calculateIntegrityHash(
                lastHash, actorName, productCode, eventType, metadata);

        SignedTimestamp signedTimestamp = timestampService.signTimestamp(currentHash);

        ProductChain productChain = new ProductChain(
                actorName, productCode, eventType, metadata,
                lastHash, signatureBase64, actor.getPublicKey(), currentHash,
                signedTimestamp.timestamp(), signedTimestamp.signature());

        return productChainRepository.save(productChain);
    }

    public boolean verifyChainIntegrity(String productCode) {
        List<ProductChain> events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(productCode);

        if (events.isEmpty()) {
            return false;
        }

        String previousHash = null;

        for (ProductChain event : events) {
            if (!Objects.equals(event.getPreviousHash(), previousHash)) {
                return false;
            }

            try {
                String expectedHash = hashService.calculateIntegrityHash(
                        previousHash, event.getActor(), event.getProductCode(),
                        event.getEventType(), event.getMetadata());

                if (!expectedHash.equals(event.getCurrentHash())) {
                    return false;
                }

                PublicKey publicKey = cryptoService.loadPublicKeyFromPem(event.getPublicKeySnapshot());
                String data = (previousHash == null ? "BEGIN" : previousHash)
                        + event.getActor()
                        + event.getProductCode()
                        + event.getEventType()
                        + event.getMetadata();

                boolean validSignature = cryptoService.verifySignature(
                        data.getBytes(StandardCharsets.UTF_8), event.getSignature(), publicKey);

                if (!validSignature) {
                    return false;
                }

                SignedTimestamp signedTimestamp = new SignedTimestamp(
                        event.getTrustedTimestamp(), event.getTimestampSignature());
                if (!timestampService.verifyTimestamp(event.getCurrentHash(), signedTimestamp)) {
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