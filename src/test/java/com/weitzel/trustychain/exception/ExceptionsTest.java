package com.weitzel.trustychain.exception;

import com.weitzel.trustychain.common.exception.Exceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    @DisplayName("Should create ActorNotFoundException")
    void shouldCreateActorNotFoundException() {
        var ex = new Exceptions.ActorNotFoundException("Actor not found");
        assertEquals("Actor not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should create ActorAlreadyExistsException")
    void shouldCreateActorAlreadyExistsException() {
        var ex = new Exceptions.ActorAlreadyExistsException("Actor exists");
        assertEquals("Actor exists", ex.getMessage());
    }

    @Test
    @DisplayName("Should create InvalidSignatureException")
    void shouldCreateInvalidSignatureException() {
        var ex = new Exceptions.InvalidSignatureException("Invalid sig");
        assertEquals("Invalid sig", ex.getMessage());
    }

    @Test
    @DisplayName("Should create ChainIntegrityException")
    void shouldCreateChainIntegrityException() {
        var ex = new Exceptions.ChainIntegrityException("Chain broken");
        assertEquals("Chain broken", ex.getMessage());
    }

    @Test
    @DisplayName("Should create ProductNotFoundException")
    void shouldCreateProductNotFoundException() {
        var ex = new Exceptions.ProductNotFoundException("Product not found");
        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    @DisplayName("ActorNotFoundException extends RuntimeException")
    void actorNotFoundExceptionExtendsRuntimeException() {
        var ex = new Exceptions.ActorNotFoundException("msg");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("InvalidSignatureException extends RuntimeException")
    void invalidSignatureExceptionExtendsRuntimeException() {
        var ex = new Exceptions.InvalidSignatureException("msg");
        assertTrue(ex instanceof RuntimeException);
    }
}
