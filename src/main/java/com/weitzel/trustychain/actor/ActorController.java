package com.weitzel.trustychain.actor;

import com.weitzel.trustychain.actor.dto.ActorRequest;
import com.weitzel.trustychain.chain.ProductChain;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actors")
public class ActorController {
    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @PostMapping
    public ResponseEntity<Actor> createActor(@Valid @RequestBody ActorRequest dto) {
        Actor actor = actorService.registerActor(dto.name(), dto.username(), dto.password(), dto.role(),
                dto.publicKey());
        return ResponseEntity.ok(actor);
    }

    @GetMapping("/{actorId}/signatures")
    public ResponseEntity<List<Map<String, String>>> getActorSignatures(@PathVariable UUID actorId) {
        List<ProductChain> events = actorService.getEventsByActor(actorId);

        List<Map<String, String>> signatures = events.stream()
                .map(event -> Map.of(
                        "product", event.getProductCode(),
                        "timestamp", event.getCreatedAt().toString(),
                        "hash", event.getCurrentHash()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(signatures);
    }

    @GetMapping
    public ResponseEntity<List<Actor>> getAllActors() {
        List<Actor> actors = actorService.findAllActors();
        return ResponseEntity.ok(actors);
    }

    @GetMapping("/{actorId}")
    public ResponseEntity<Actor> getActorById(@PathVariable UUID actorId) {
        Actor actor = actorService.findActorById(actorId);
        return ResponseEntity.ok(actor);
    }

    @PutMapping("/{actorId}")
    public ResponseEntity<Actor> updateActor(@PathVariable UUID actorId,
            @Valid @RequestBody ActorRequest actorDetails) {
        Actor updatedActor = actorService.updateActor(actorId, actorDetails);
        return ResponseEntity.ok(updatedActor);
    }

    @DeleteMapping("/{actorId}")
    public ResponseEntity<Void> deleteActor(@PathVariable UUID actorId) {
        actorService.deleteActor(actorId);
        return ResponseEntity.noContent().build();
    }
}