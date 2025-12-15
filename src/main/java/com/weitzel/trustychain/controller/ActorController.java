package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.model.DTO.ActorRequest;
import com.weitzel.trustychain.model.entity.Actor;
import com.weitzel.trustychain.model.entity.ProductChain;
import com.weitzel.trustychain.service.ActorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actors")
public class ActorController {
    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @PostMapping
    public ResponseEntity<Actor> createActor(@RequestBody ActorRequest dto) {
        Actor actor = actorService.registerActor(dto.name(), dto.role());
        return ResponseEntity.ok(actor);
    }

    @GetMapping("/{actorId}/signatures")
    public ResponseEntity<List<Map<String, String>>> getActorSignatures(@PathVariable String actorId) {

        List<ProductChain> events = actorService.getEventsByActor(actorId);

        List<Map<String, String>> signatures = events.stream()
                .map(event -> Map.of(
                        "product", event.getProductCode(),
                        "timestamp", event.getCreatedAt().toString(),
                        "hash", event.getCurrentHash()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(signatures);
    }
}