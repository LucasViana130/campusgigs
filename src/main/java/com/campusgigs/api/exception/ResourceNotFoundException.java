package com.campusgigs.api.exception;

/** Lancada quando um recurso solicitado nao existe (404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
