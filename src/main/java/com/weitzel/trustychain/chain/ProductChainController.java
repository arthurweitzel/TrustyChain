package com.weitzel.trustychain.chain;

import com.weitzel.trustychain.chain.dto.EventChainRequest;
import com.weitzel.trustychain.chain.dto.TrackingResponse;
import com.weitzel.trustychain.chain.dto.TrackingResponse.ChainEventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-chain")
@Tag(name = "Product Chain", description = "Product chain management and tracking")
public class ProductChainController {

    private final ProductChainService productChainService;
    private final ProductChainRepository productChainRepository;
    private final TrackingService trackingService;

    public ProductChainController(ProductChainService productChainService,
            ProductChainRepository productChainRepository,
            TrackingService trackingService) {
        this.productChainService = productChainService;
        this.productChainRepository = productChainRepository;
        this.trackingService = trackingService;
    }

    @PostMapping("/event")
    @Operation(summary = "Register new event", description = "Registers a new event in the product chain")
    public ResponseEntity<ProductChain> registerEvent(@Valid @RequestBody EventChainRequest request) {
        ProductChain saved = productChainService.registerEvent(
                request.actor(),
                request.productCode(),
                request.eventType(),
                request.metadata(),
                request.signature());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{query}")
    @Operation(summary = "Get product tracking history", description = "Returns the complete chain history for a product. Searches by product code, hash, or UUID.")
    public ResponseEntity<TrackingResponse> getProductTracking(@PathVariable String query) {
        List<ProductChain> events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(query);

        if (events.isEmpty()) {
            Optional<ProductChain> byHash = productChainRepository.findByCurrentHash(query);
            if (byHash.isPresent()) {
                events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(byHash.get().getProductCode());
            }
        }

        if (events.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(query);
                Optional<ProductChain> byId = productChainRepository.findById(uuid);
                if (byId.isPresent()) {
                    events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(byId.get().getProductCode());
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String productCode = events.get(0).getProductCode();
        boolean isValid = productChainService.verifyChainIntegrity(productCode);

        List<ChainEventDTO> eventDTOs = events.stream()
                .map(event -> new ChainEventDTO(
                        event.getActor(),
                        event.getEventType(),
                        event.getMetadata(),
                        event.getTrustedTimestamp(),
                        event.getCurrentHash()))
                .toList();

        String qrCodeUrl = trackingService.generateQRCodeUrl(productCode);

        return ResponseEntity.ok(new TrackingResponse(productCode, isValid, eventDTOs, qrCodeUrl));
    }

    @GetMapping(value = "/{productCode}/last-hash", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get last hash for product", description = "Returns the last hash in the chain for signing new events. Returns empty string for new products.")
    public ResponseEntity<String> getLastHash(@PathVariable String productCode) {
        return ResponseEntity.ok(
                productChainRepository.findTopByProductCodeOrderByCreatedAtDesc(productCode)
                        .map(ProductChain::getCurrentHash)
                        .orElse(""));
    }

    @GetMapping("/{productCode}/qr")
    @Operation(summary = "Get QR code image", description = "Returns a QR code PNG image that links to the product tracking page")
    public ResponseEntity<byte[]> getQRCode(
            @PathVariable String productCode,
            @RequestParam(defaultValue = "300") int size) {

        List<ProductChain> events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(productCode);
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] qrCode = trackingService.generateQRCode(productCode, size, size);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    @GetMapping("/{productCode}/verify")
    @Operation(summary = "Verify chain integrity", description = "Verifies the integrity of the entire product chain")
    public ResponseEntity<VerificationResult> verifyChain(@PathVariable String productCode) {
        List<ProductChain> events = productChainRepository.findByProductCodeOrderByCreatedAtAsc(productCode);

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean isValid = productChainService.verifyChainIntegrity(productCode);
        int eventCount = events.size();

        return ResponseEntity.ok(new VerificationResult(productCode, isValid, eventCount));
    }

    public record VerificationResult(String productCode, boolean isValid, int eventCount) {
    }
}