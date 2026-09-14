package com.campusgigs.api.exception;

/** Lancada quando o CEP informado tem formato valido mas nao existe (400). */
public class CepNotFoundException extends RuntimeException {
    public CepNotFoundException(String cep) {
        super("CEP nao encontrado: " + cep);
    }
}
