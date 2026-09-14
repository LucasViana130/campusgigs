package com.campusgigs.api.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

/**
 * Cliente HTTP DECLARATIVO (Spring HttpExchange) para o servico externo
 * publico de CEP. Nao ha nenhuma chamada HTTP manual/RestTemplate aqui: a
 * implementacao concreta e gerada pelo HttpServiceProxyFactory configurado
 * em {@link com.campusgigs.api.config.RestClientConfig}.
 */
public interface ViaCepClient {

    @GetExchange("/ws/{cep}/json/")
    ViaCepResponse buscarCep(@PathVariable("cep") String cep);
}
