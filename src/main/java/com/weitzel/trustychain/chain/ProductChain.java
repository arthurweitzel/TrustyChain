package com.weitzel.trustychain.chain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_chain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductChain {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "current_hash", nullable = false)
    private String currentHash;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "public_key_snapshot", nullable = false)
    private String publicKeySnapshot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "trusted_timestamp", nullable = false)
    private LocalDateTime trustedTimestamp;

    @Column(name = "timestamp_signature", nullable = false)
    private String timestampSignature;

    // new event, timestamp grants proof of time
    public ProductChain(String actor, String productCode, String eventType, String metadata,
            String previousHash, String signature, String publicKeySnapshot, String currentHash,
            LocalDateTime trustedTimestamp, String timestampSignature) {
        this.actor = actor;
        this.productCode = productCode;
        this.eventType = eventType;
        this.metadata = metadata;
        this.createdAt = LocalDateTime.now();
        this.previousHash = previousHash;
        this.signature = signature;
        this.publicKeySnapshot = publicKeySnapshot;
        this.currentHash = currentHash;
        this.trustedTimestamp = trustedTimestamp;
        this.timestampSignature = timestampSignature;
    }
}