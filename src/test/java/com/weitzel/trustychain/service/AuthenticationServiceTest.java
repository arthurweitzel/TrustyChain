package com.weitzel.trustychain.service;

import com.weitzel.trustychain.auth.AuthenticationService;
import com.weitzel.trustychain.auth.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should authenticate user and return token")
    void shouldAuthenticateUser() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtService.generateToken(mockAuth)).thenReturn("jwt.token.here");

        String token = authenticationService.authenticate("user", "password");

        assertEquals("jwt.token.here", token);
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(mockAuth);
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void shouldThrowExceptionForInvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate("user", "wrongpassword"));
    }

    @Test
    @DisplayName("Should pass correct credentials to authentication manager")
    void shouldPassCorrectCredentials() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtService.generateToken(any())).thenReturn("token");

        authenticationService.authenticate("testuser", "testpass");

        verify(authenticationManager).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getPrincipal().equals("testuser") &&
                        auth.getCredentials().equals("testpass")));
    }
}
