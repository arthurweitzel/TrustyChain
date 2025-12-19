package com.weitzel.trustychain.service;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorRepository;
import com.weitzel.trustychain.actor.ActorService;
import com.weitzel.trustychain.chain.ProductChain;
import com.weitzel.trustychain.chain.ProductChainRepository;
import com.weitzel.trustychain.common.exception.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private ProductChainRepository productChainRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ActorService actorService;

    private Actor testActor;

    @BeforeEach
    void setUp() {
        testActor = new Actor();
        testActor.setId(UUID.randomUUID());
        testActor.setName("Test Actor");
        testActor.setUsername("testuser");
        testActor.setPassword("encodedPassword");
        testActor.setRole("PRODUCER");
        testActor.setPublicKey("-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----");
    }

    @Test
    @DisplayName("Should register new actor successfully")
    void shouldRegisterActor() {
        when(actorRepository.findByName("New Actor")).thenReturn(Optional.empty());
        when(actorRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        Actor result = actorService.registerActor(
                "New Actor", "newuser", "password", "PRODUCER", "publicKey");

        assertNotNull(result);
        assertEquals("New Actor", result.getName());
        assertEquals("newuser", result.getUsername());
        verify(actorRepository).save(any(Actor.class));
    }

    @Test
    @DisplayName("Should throw exception if actor name exists")
    void shouldThrowExceptionIfNameExists() {
        when(actorRepository.findByName("Test Actor")).thenReturn(Optional.of(testActor));

        assertThrows(Exceptions.ActorAlreadyExistsException.class,
                () -> actorService.registerActor("Test Actor", "new", "pass", "ROLE", "key"));
    }

    @Test
    @DisplayName("Should throw exception if username exists")
    void shouldThrowExceptionIfUsernameExists() {
        when(actorRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(actorRepository.findByUsername("testuser")).thenReturn(Optional.of(testActor));

        assertThrows(Exceptions.ActorAlreadyExistsException.class,
                () -> actorService.registerActor("New Name", "testuser", "pass", "ROLE", "key"));
    }

    @Test
    @DisplayName("Should find actor by ID")
    void shouldFindActorById() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));

        Actor result = actorService.findActorById(testActor.getId());

        assertEquals(testActor.getName(), result.getName());
    }

    @Test
    @DisplayName("Should throw exception if actor not found")
    void shouldThrowExceptionIfActorNotFound() {
        UUID randomId = UUID.randomUUID();
        when(actorRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(Exceptions.ActorNotFoundException.class,
                () -> actorService.findActorById(randomId));
    }

    @Test
    @DisplayName("Should find all actors")
    void shouldFindAllActors() {
        when(actorRepository.findAll()).thenReturn(List.of(testActor));

        List<Actor> result = actorService.findAllActors();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get events by actor")
    void shouldGetEventsByActor() {
        ProductChain event = new ProductChain();
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(productChainRepository.findByActor(testActor.getName())).thenReturn(List.of(event));

        List<ProductChain> result = actorService.getEventsByActor(testActor.getId());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should delete actor")
    void shouldDeleteActor() {
        when(actorRepository.existsById(testActor.getId())).thenReturn(true);

        actorService.deleteActor(testActor.getId());

        verify(actorRepository).deleteById(testActor.getId());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent actor")
    void shouldThrowExceptionWhenDeletingNonExistentActor() {
        UUID randomId = UUID.randomUUID();
        when(actorRepository.existsById(randomId)).thenReturn(false);

        assertThrows(Exceptions.ActorNotFoundException.class,
                () -> actorService.deleteActor(randomId));
    }

    @Test
    @DisplayName("Should update actor name")
    void shouldUpdateActorName() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.findByName("Updated Name")).thenReturn(Optional.empty());
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                "Updated Name", null, null, null, null);
        Actor result = actorService.updateActor(testActor.getId(), request);

        assertEquals("Updated Name", result.getName());
    }

    @Test
    @DisplayName("Should update actor username")
    void shouldUpdateActorUsername() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.findByUsername("newusername")).thenReturn(Optional.empty());
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                null, "newusername", null, null, null);
        Actor result = actorService.updateActor(testActor.getId(), request);

        assertEquals("newusername", result.getUsername());
    }

    @Test
    @DisplayName("Should update actor password")
    void shouldUpdateActorPassword() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                null, null, "newpassword", null, null);
        Actor result = actorService.updateActor(testActor.getId(), request);

        assertEquals("encodedNewPassword", result.getPassword());
    }

    @Test
    @DisplayName("Should update actor role and public key")
    void shouldUpdateActorRoleAndPublicKey() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                null, null, null, "ADMIN", "newPublicKey");
        Actor result = actorService.updateActor(testActor.getId(), request);

        assertEquals("ADMIN", result.getRole());
        assertEquals("newPublicKey", result.getPublicKey());
    }

    @Test
    @DisplayName("Should throw exception when updating to existing name")
    void shouldThrowExceptionWhenUpdatingToExistingName() {
        Actor otherActor = new Actor();
        otherActor.setId(UUID.randomUUID());
        otherActor.setName("Existing Name");

        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.findByName("Existing Name")).thenReturn(Optional.of(otherActor));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                "Existing Name", null, null, null, null);

        assertThrows(Exceptions.ActorAlreadyExistsException.class,
                () -> actorService.updateActor(testActor.getId(), request));
    }

    @Test
    @DisplayName("Should throw exception when updating to existing username")
    void shouldThrowExceptionWhenUpdatingToExistingUsername() {
        Actor otherActor = new Actor();
        otherActor.setId(UUID.randomUUID());
        otherActor.setUsername("existinguser");

        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.findByUsername("existinguser")).thenReturn(Optional.of(otherActor));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                null, "existinguser", null, null, null);

        assertThrows(Exceptions.ActorAlreadyExistsException.class,
                () -> actorService.updateActor(testActor.getId(), request));
    }

    @Test
    @DisplayName("Should allow updating to same name for same actor")
    void shouldAllowUpdatingToSameNameForSameActor() {
        when(actorRepository.findById(testActor.getId())).thenReturn(Optional.of(testActor));
        when(actorRepository.findByName(testActor.getName())).thenReturn(Optional.of(testActor));
        when(actorRepository.save(any(Actor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new com.weitzel.trustychain.actor.dto.ActorRequest(
                testActor.getName(), null, null, null, null);
        Actor result = actorService.updateActor(testActor.getId(), request);

        assertNotNull(result);
    }
}
