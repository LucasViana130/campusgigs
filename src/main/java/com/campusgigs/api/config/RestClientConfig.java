package com.campusgigs.api.config;

import com.campusgigs.api.client.ViaCepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuracao do cliente HTTP declarativo (Spring HttpExchange) usado para
 * consultar o servico externo de CEP.
 *
 * Decisao de implementacao: a URL base, o timeout de conexao e o timeout de
 * leitura sao configuraveis via propriedades/variaveis de ambiente
 * (cep.api.base-url, cep.connect-timeout-ms, cep.read-timeout-ms), com
 * defaults razoaveis para desenvolvimento. Nao ha nenhum uso de RestTemplate
 * "cru" ou chamada HTTP manual em nenhum outro ponto do projeto: toda a
 * integracao externa passa por esta interface declarativa.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public ViaCepClient viaCepClient(
            @Value("${cep.api.base-url}") String baseUrl,
            @Value("${cep.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${cep.read-timeout-ms}") int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();

        return proxyFactory.createClient(ViaCepClient.class);
    }
}
