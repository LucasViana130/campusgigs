package com.campusgigs.api.exception;

/** Lancada quando o e-mail informado no cadastro ja pertence a outro usuario (409). */
public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("Ja existe um usuario cadastrado com o e-mail '" + email + "'");
    }
}
