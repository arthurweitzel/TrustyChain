package com.weitzel.trustychain.service;

import com.weitzel.trustychain.model.entity.Actor;
import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.repository.ActorRepository;
import com.weitzel.trustychain.repository.ProductChainRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActorService {
    private final ActorRepository actorRepository;
    private final ProductChainRepository productChainRepository;
    private final PasswordEncoder passwordEncoder;

    public ActorService(ActorRepository actorRepository, ProductChainRepository productChainRepository, PasswordEncoder passwordEncoder) {
        this.actorRepository = actorRepository;
        this.productChainRepository = productChainRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Actor registerActor(String name, String username, String password, String role, String publicKey) {
        if (actorRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Actor with this name already exists");
        }
        if (actorRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Actor with this username already exists");
        }
        Actor newActor = new Actor();
        newActor.setName(name);
        newActor.setUsername(username);
        newActor.setPassword(passwordEncoder.encode(password));
        newActor.setRole(role);
        newActor.setPublicKey(publicKey);
        return actorRepository.save(newActor);
    }

    public List<ProductChain> getEventsByActor(UUID actorId) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        return productChainRepository.findByActor(actor.getName());
    }
}
