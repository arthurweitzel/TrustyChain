package com.weitzel.trustychain.actor;

import com.weitzel.trustychain.actor.dto.ActorRequest;
import com.weitzel.trustychain.chain.ProductChain;
import com.weitzel.trustychain.chain.ProductChainRepository;
import com.weitzel.trustychain.common.exception.Exceptions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActorService {
    private final ActorRepository actorRepository;
    private final ProductChainRepository productChainRepository;
    private final PasswordEncoder passwordEncoder;

    public ActorService(ActorRepository actorRepository, ProductChainRepository productChainRepository,
            PasswordEncoder passwordEncoder) {
        this.actorRepository = actorRepository;
        this.productChainRepository = productChainRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Actor registerActor(String name, String username, String password, String role, String publicKey) {
        if (actorRepository.findByName(name).isPresent()) {
            throw new Exceptions.ActorAlreadyExistsException("Actor with this name already exists");
        }
        if (actorRepository.findByUsername(username).isPresent()) {
            throw new Exceptions.ActorAlreadyExistsException("Actor with this username already exists");
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
                .orElseThrow(() -> new Exceptions.ActorNotFoundException("Actor not found"));
        return productChainRepository.findByActor(actor.getName());
    }

    public List<Actor> findAllActors() {
        return actorRepository.findAll();
    }

    public Actor findActorById(UUID actorId) {
        return actorRepository.findById(actorId)
                .orElseThrow(() -> new Exceptions.ActorNotFoundException("Actor not found"));
    }

    public Actor updateActor(UUID actorId, ActorRequest actorDetails) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new Exceptions.ActorNotFoundException("Actor not found"));

        if (actorDetails.name() != null) {
            actorRepository.findByName(actorDetails.name()).ifPresent(existingActor -> {
                if (!existingActor.getId().equals(actorId)) {
                    throw new Exceptions.ActorAlreadyExistsException("Actor with this name already exists");
                }
            });
            actor.setName(actorDetails.name());
        }

        if (actorDetails.username() != null) {
            actorRepository.findByUsername(actorDetails.username()).ifPresent(existingActor -> {
                if (!existingActor.getId().equals(actorId)) {
                    throw new Exceptions.ActorAlreadyExistsException("Actor with this username already exists");
                }
            });
            actor.setUsername(actorDetails.username());
        }

        if (actorDetails.password() != null) {
            actor.setPassword(passwordEncoder.encode(actorDetails.password()));
        }
        if (actorDetails.role() != null) {
            actor.setRole(actorDetails.role());
        }
        if (actorDetails.publicKey() != null) {
            actor.setPublicKey(actorDetails.publicKey());
        }
        return actorRepository.save(actor);
    }

    public void deleteActor(UUID actorId) {
        if (!actorRepository.existsById(actorId)) {
            throw new Exceptions.ActorNotFoundException("Actor not found");
        }
        actorRepository.deleteById(actorId);
    }
}