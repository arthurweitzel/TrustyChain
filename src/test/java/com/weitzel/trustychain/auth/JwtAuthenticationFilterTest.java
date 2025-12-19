package com.weitzel.trustychain.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    @Test
    @DisplayName("Should create filter instance")
    void shouldCreateFilterInstance() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
        jwtService.init();

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, null);
        assertNotNull(filter);
    }
}
