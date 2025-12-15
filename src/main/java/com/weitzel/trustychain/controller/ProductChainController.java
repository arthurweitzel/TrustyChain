package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.EventChainRequest;
import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.service.ProductChainService;
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
    public ResponseEntity<ProductChain> registerEvent(@RequestBody EventChainRequest request) {
        ProductChain saved = productChainService.registerEvent(
                request.actor(),
                request.productCode(),
                request.eventType(),
                request.metadata(),
                request.signature()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/verify/{productCode}")
    public ResponseEntity<Boolean> verifyChain(@PathVariable String productCode) {
        boolean valid = productChainService.verifyChainIntegrity(productCode);
        return ResponseEntity.ok(valid);
    }
}
