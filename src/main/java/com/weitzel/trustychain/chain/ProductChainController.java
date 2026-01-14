package com.weitzel.trustychain.chain;

import com.weitzel.trustychain.chain.dto.EventChainRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-chain")
public class ProductChainController {

    private final ProductChainService productChainService;

    public ProductChainController(ProductChainService productChainService) {
        this.productChainService = productChainService;
    }

    @PostMapping("/event")
    public ResponseEntity<ProductChain> registerEvent(@Valid @RequestBody EventChainRequest request) {
        ProductChain saved = productChainService.registerEvent(
                request.actor(),
                request.productCode(),
                request.eventType(),
                request.metadata(),
                request.signature());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/verify/{productCode}")
    public ResponseEntity<Boolean> verifyChain(@PathVariable String productCode) {
        boolean valid = productChainService.verifyChainIntegrity(productCode);
        return ResponseEntity.ok(valid);
    }
}