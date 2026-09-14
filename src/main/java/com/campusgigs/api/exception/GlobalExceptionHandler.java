package com.campusgigs.api.exception;

import com.campusgigs.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de erros da API.
 *
 * Objetivo: toda resposta de erro segue o mesmo formato (ApiErrorResponse) e
 * nunca expoe stack trace, nome de classe interna, SQL ou qualquer detalhe
 * de implementacao ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- 400: validacao de campos (Bean Validation) ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ApiErrorResponse body = ApiErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                "Requisicao invalida",
                "Um ou mais campos estao invalidos",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ---- 403: autenticado, porem sem permissao para a operacao ----
    // Nota: quando AccessDeniedException e lancada de dentro de um controller/service
    // (como as regras de propriedade em GigService), o Spring MVC resolve o
    // @ExceptionHandler ANTES de a excecao chegar ao AccessDeniedHandler do
    // Spring Security. Por isso ela tambem e tratada aqui, com o mesmo formato
    // usado pelo RestAccessDeniedHandler (que continua cobrindo negacoes que
    // ocorrem diretamente na cadeia de filtros de seguranca).
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                                HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(), "Acesso negado", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ---- 400: parametro de request com tipo/valor incompativel (ex.: ?status=XYZ) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        String message = "Valor invalido para o parametro '" + ex.getName() + "'";
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Requisicao invalida", message, request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // ---- 400: regra de negocio violada ----
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleViolationException ex,
                                                                HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Regra de negocio violada", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // ---- 400: CEP com formato valido mas inexistente ----
    @ExceptionHandler(CepNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCepNotFound(CepNotFoundException ex,
                                                               HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "CEP invalido", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // ---- 502: servico externo de CEP indisponivel/timeout ----
    @ExceptionHandler(CepServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleCepServiceUnavailable(CepServiceUnavailableException ex,
                                                                         HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(), "Servico externo indisponivel", ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    // ---- 404: recurso inexistente ----
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                            HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "Nao encontrado", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ---- 409: e-mail duplicado ----
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailInUse(EmailAlreadyInUseException ex,
                                                              HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(), "Conflito", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ---- 401: credenciais invalidas no login ----
    // Mensagem generica de proposito: nao revela se o problema foi o e-mail
    // inexistente ou a senha incorreta (evita enumeracao de usuarios).
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                                  HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(), "Nao autenticado",
                "E-mail ou senha invalidos", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // ---- 500: fallback - nunca vaza detalhe interno ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro nao tratado ao processar {} {}", request.getMethod(), request.getRequestURI(), ex);
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(body);
    }
}
