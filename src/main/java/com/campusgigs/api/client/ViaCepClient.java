package com.campusgigs.api.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Cliente HTTP DECLARATIVO (Spring HttpExchange) para o servico externo
 * publico de CEP. Nao ha nenhuma chamada HTTP manual/RestTemplate aqui: a
 * implementacao concreta e gerada pelo HttpServiceProxyFactory configurado
 * em {@link com.campusgigs.api.config.RestClientConfig}.
 *
 * O {@code @HttpExchange} no nivel da interface declara o Accept padrao,
 * herdado por todo metodo de exchange (aqui, o {@code @GetExchange}, que e
 * apenas um atalho de {@code @HttpExchange} para o metodo GET) - deixando
 * explicito, ja na assinatura da interface, que se trata de um cliente
 * HttpExchange.
 */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface ViaCepClient {

    @GetExchange("/ws/{cep}/json/")
    ViaCepResponse buscarCep(@PathVariable("cep") String cep);
}
