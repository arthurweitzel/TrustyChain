package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.repository.ProductChainRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProductChainService {
    private final ProductChainRepository productChainRepository;

    public ProductChainService(ProductChainRepository productChainRepository) {
        this.productChainRepository = productChainRepository;
    }

    // transactional secures that if the change couldnt be accepted it will be rollbacked
    @Transactional
    public ProductChain registerEvent(String actor, String productCode, String eventType, String metadata) {
        String lastHash = productChainRepository.findTopByProductCodeOrderByIdDesc(productCode)
                .map(ProductChain::getCurrentHash)
                .orElse(null);

        ProductChain productChain = new ProductChain(actor, productCode, eventType, metadata, lastHash);
        return productChainRepository.save(productChain);
    }

    // to do
    public boolean verifyChainIntegrity(String productCode) {
        return true;
    }
}
