package com.campusgigs.api.client;

import com.campusgigs.api.exception.CepNotFoundException;
import com.campusgigs.api.exception.CepServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Camada de servico que isola o restante da aplicacao dos detalhes da
 * integracao externa de CEP.
 *
 * Decisao de implementacao (relevante para a justificativa do commit do CP5):
 * o cliente HttpExchange (ViaCepClient) pode falhar de tres formas distintas,
 * e cada uma e tratada de um jeito diferente para nunca deixar o cadastro
 * silenciosamente incompleto:
 *
 *   1. CEP com formato valido mas inexistente -> ViaCEP responde HTTP 200
 *      com corpo {"erro": true}. Traduzido para CepNotFoundException (400):
 *      o problema e do dado enviado pelo cliente.
 *   2. Timeout ou falha de conexao (servico fora do ar, rede instavel) ->
 *      ResourceAccessException. Traduzido para CepServiceUnavailableException
 *      (502): o problema e do servico externo, nao do cliente da nossa API.
 *   3. Resposta HTTP de erro do proprio ViaCEP (4xx/5xx) ou corpo
 *      inesperado/nao parseavel -> tambem tratado como servico externo
 *      indisponivel (502), sem vazar o corpo/erro original ao cliente final.
 */
@Service
@RequiredArgsConstructor
public class CepLookupService {

    private static final Logger log = LoggerFactory.getLogger(CepLookupService.class);

    private final ViaCepClient viaCepClient;

    public CepLookupResult lookup(String cep) {
        ViaCepResponse response;

        try {
            response = viaCepClient.buscarCep(cep);
        } catch (ResourceAccessException e) {
            // Timeout de conexao/leitura ou host inalcancavel.
            log.warn("Timeout/indisponibilidade ao consultar CEP {} no servico externo", cep, e);
            throw new CepServiceUnavailableException(
                    "Servico externo de CEP indisponivel no momento. Tente novamente em instantes.");
        } catch (RestClientResponseException e) {
            // O servico externo respondeu, mas com um status de erro HTTP.
            log.warn("Servico externo de CEP retornou status {} para o CEP {}", e.getStatusCode(), cep);
            throw new CepServiceUnavailableException(
                    "Servico externo de CEP retornou um erro ao consultar o CEP informado.");
        } catch (RestClientException e) {
            // Qualquer outra falha de comunicacao/parsing nao coberta acima.
            log.warn("Falha inesperada ao consultar CEP {} no servico externo", cep, e);
            throw new CepServiceUnavailableException(
                    "Nao foi possivel consultar o servico externo de CEP no momento.");
        }

        if (response == null || response.isNotFound()) {
            throw new CepNotFoundException(cep);
        }

        return new CepLookupResult(response.localidade(), response.uf());
    }
}
