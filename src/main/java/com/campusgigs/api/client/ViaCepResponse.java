package com.campusgigs.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta do servico externo ViaCEP (https://viacep.com.br).
 * Quando o CEP tem formato valido mas nao existe, a API responde HTTP 200
 * com o corpo {"erro": true} - por isso o campo "erro" precisa ser checado
 * explicitamente, e nao apenas o status HTTP.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
) {
    public boolean isNotFound() {
        return Boolean.TRUE.equals(erro);
    }
}
