package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.auth.AuthenticationController;
import com.weitzel.trustychain.auth.AuthenticationService;
import com.weitzel.trustychain.auth.JwtAuthenticationFilter;
import com.weitzel.trustychain.auth.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        when(authenticationService.authenticate("user", "password"))
                .thenReturn("jwt.token.here");

        String requestBody = """
                {
                    "username": "user",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"));
    }

    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        when(authenticationService.authenticate(any(), any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        String requestBody = """
                {
                    "username": "user",
                    "password": "wrong"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
