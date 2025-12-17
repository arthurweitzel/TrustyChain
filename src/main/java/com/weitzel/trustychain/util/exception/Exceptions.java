package com.weitzel.trustychain.util.exception;

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
}
