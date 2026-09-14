package com.campusgigs.api.exception;

/**
 * Lancada quando o servico externo de CEP nao responde a tempo (timeout) ou
 * esta indisponivel/retorna uma resposta invalida (502).
 * O erro interno completo (causa) nunca e exposto ao cliente final.
 */
public class CepServiceUnavailableException extends RuntimeException {
    public CepServiceUnavailableException(String message) {
        super(message);
    }
}
