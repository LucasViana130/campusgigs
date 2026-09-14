package com.campusgigs.api.client;

/** Resultado interno, ja normalizado, de uma consulta de CEP bem-sucedida. */
public record CepLookupResult(String city, String state) {
}
