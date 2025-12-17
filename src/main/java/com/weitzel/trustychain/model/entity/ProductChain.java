package com.weitzel.trustychain.model.entity;

import com.weitzel.trustychain.util.helper.ChainHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_chain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductChain {
    @jakarta.persistence.Id
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

    @Column(name = "previous_hash") // this can be null if its the 1st in the chain
    private String previousHash;

    @Column(name = "current_hash", nullable = false)
    private String currentHash;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "public_key_snapshot", nullable = false)
    private String publicKeySnapshot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProductChain
            (String actor,
             String productCode,
             String eventType,
             String metadata,
             String previousHash,
             String signature,
             String publicKeySnapshot
             ) {
        this.actor = actor;
        this.productCode = productCode;
        this.eventType = eventType;
        this.metadata = metadata;
        this.createdAt = LocalDateTime.now();
        this.previousHash = previousHash;
        this.signature = signature;
        this.publicKeySnapshot = publicKeySnapshot;
    }

    @PrePersist
    public void calculateIntegrityHash() {
        if (this.currentHash != null) return;

        try {
            String data = (previousHash == null ? "BEGIN" : previousHash) +
                     actor +
                     productCode +
                     eventType +
                     metadata;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            this.currentHash = ChainHelper.bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("couldnt generate a integrity hash", e);
        }
    }
}
