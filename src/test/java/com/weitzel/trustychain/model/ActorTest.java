package com.weitzel.trustychain.model;

import com.weitzel.trustychain.actor.Actor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    @Test
    @DisplayName("Should create actor with all fields")
    void shouldCreateActorWithAllFields() {
        Actor actor = new Actor();
        UUID id = UUID.randomUUID();
        actor.setId(id);
        actor.setName("Test Actor");
        actor.setUsername("testuser");
        actor.setPassword("password123");
        actor.setRole("PRODUCER");
        actor.setPublicKey("publicKey");

        assertEquals(id, actor.getId());
        assertEquals("Test Actor", actor.getName());
        assertEquals("testuser", actor.getUsername());
        assertEquals("password123", actor.getPassword());
        assertEquals("PRODUCER", actor.getRole());
        assertEquals("publicKey", actor.getPublicKey());
    }

    @Test
    @DisplayName("Should return true for isAccountNonExpired")
    void shouldReturnTrueForIsAccountNonExpired() {
        Actor actor = new Actor();
        assertTrue(actor.isAccountNonExpired());
    }

    @Test
    @DisplayName("Should return true for isAccountNonLocked")
    void shouldReturnTrueForIsAccountNonLocked() {
        Actor actor = new Actor();
        assertTrue(actor.isAccountNonLocked());
    }

    @Test
    @DisplayName("Should return true for isCredentialsNonExpired")
    void shouldReturnTrueForIsCredentialsNonExpired() {
        Actor actor = new Actor();
        assertTrue(actor.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Should return true for isEnabled")
    void shouldReturnTrueForIsEnabled() {
        Actor actor = new Actor();
        assertTrue(actor.isEnabled());
    }

    @Test
    @DisplayName("Should return authorities based on role")
    void shouldReturnAuthoritiesBasedOnRole() {
        Actor actor = new Actor();
        actor.setRole("ADMIN");

        var authorities = actor.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
    }

    @Test
    @DisplayName("Should use AllArgsConstructor")
    void shouldUseAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        Actor actor = new Actor(id, "Name", "username", "pass", "ROLE", "key");

        assertEquals(id, actor.getId());
        assertEquals("Name", actor.getName());
    }

    @Test
    @DisplayName("Should use NoArgsConstructor")
    void shouldUseNoArgsConstructor() {
        Actor actor = new Actor();
        assertNotNull(actor);
    }
}
