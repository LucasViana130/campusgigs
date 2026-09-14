package com.campusgigs.api.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Formato padronizado de resposta de erro da API. Nunca deve carregar stack
 * trace, nome de classes internas, SQL ou qualquer detalhe interno.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse ofValidation(int status, String error, String message, String path,
                                                 Map<String, String> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
