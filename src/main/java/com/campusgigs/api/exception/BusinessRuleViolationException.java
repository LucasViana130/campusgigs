package com.campusgigs.api.exception;

/** Lancada quando uma regra de negocio e violada (400). */
public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
