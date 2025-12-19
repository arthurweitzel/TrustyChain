package com.weitzel.trustychain.common.exception;

public class Exceptions {

    public static class ActorAlreadyExistsException extends RuntimeException {
        public ActorAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class ActorNotFoundException extends RuntimeException {
        public ActorNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidSignatureException extends RuntimeException {
        public InvalidSignatureException(String message) {
            super(message);
        }

        public InvalidSignatureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ChainIntegrityException extends RuntimeException {
        public ChainIntegrityException(String message) {
            super(message);
        }

        public ChainIntegrityException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }
}
