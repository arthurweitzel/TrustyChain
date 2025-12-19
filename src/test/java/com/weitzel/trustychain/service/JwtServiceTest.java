package com.weitzel.trustychain.service;

import com.weitzel.trustychain.auth.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
        jwtService.init();
    }

    @Test
    @DisplayName("Should generate JWT token")
    void shouldGenerateToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("PRODUCER")));

        String token = jwtService.generateToken(auth);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract claims from token")
    void shouldExtractClaims() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "fazenda",
                "password",
                List.of(new SimpleGrantedAuthority("PRODUCER")));

        String token = jwtService.generateToken(auth);
        Claims claims = jwtService.getClaims(token);

        assertEquals("fazenda", claims.getSubject());
        assertEquals("PRODUCER", claims.get("auth"));
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user",
                "pass",
                List.of(new SimpleGrantedAuthority("ADMIN")));

        String token = jwtService.generateToken(auth);
        boolean valid = jwtService.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        boolean valid = jwtService.isTokenValid("invalid.token.here");

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        jwtService.init();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", "pass", List.of(new SimpleGrantedAuthority("USER")));

        String token = jwtService.generateToken(auth);
        boolean valid = jwtService.isTokenValid(token);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should handle multiple authorities")
    void shouldHandleMultipleAuthorities() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                List.of(
                        new SimpleGrantedAuthority("ADMIN"),
                        new SimpleGrantedAuthority("PRODUCER")));

        String token = jwtService.generateToken(auth);
        Claims claims = jwtService.getClaims(token);

        String authorities = claims.get("auth", String.class);
        assertTrue(authorities.contains("ADMIN"));
        assertTrue(authorities.contains("PRODUCER"));
    }

    @Test
    @DisplayName("Should reject null token")
    void shouldRejectNullToken() {
        boolean valid = jwtService.isTokenValid(null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        boolean valid = jwtService.isTokenValid("");
        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        boolean valid = jwtService.isTokenValid("not.a.valid.jwt.token");
        assertFalse(valid);
    }

    @Test
    @DisplayName("Should initialize with configured secret")
    void shouldInitializeWithConfiguredSecret() {
        JwtService configuredService = new JwtService();
        String validSecret = "dGVzdFNlY3JldEtleVRoYXRJc0F0TGVhc3QzMkJ5dGVz";
        ReflectionTestUtils.setField(configuredService, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(configuredService, "expiration", 86400000L);

        assertDoesNotThrow(() -> configuredService.init());
    }
}
