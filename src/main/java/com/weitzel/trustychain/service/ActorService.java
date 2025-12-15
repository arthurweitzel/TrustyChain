package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.entity.Actor;
import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.repository.ActorRepository;
import com.weitzel.trustychain.repository.ProductChainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActorService {
    private final ActorRepository actorRepository;
    private final ProductChainRepository productChainRepository;

    public ActorService(ActorRepository actorRepository, ProductChainRepository productChainRepository) {
        this.actorRepository = actorRepository;
        this.productChainRepository = productChainRepository;
    }

    public Actor registerActor(String name, String role) {
        Actor newActor = new Actor();
        return actorRepository.save(newActor);
    }

    public List<ProductChain> getEventsByActor(String actorId) {
        // gambiarra braba pesada alucinogena
        UUID uuid;
        try {
            uuid = UUID.fromString(actorId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid UUID format");
        }
        if (!actorRepository.existsById(uuid))
            throw new RuntimeException("Actor not found");
        return productChainRepository.findByActorId(actorId);
    }
}
