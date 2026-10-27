package com.enterprise.ai.gateway.exception;

import com.enterprise.ai.common.dto.ErrorResponse;
import com.enterprise.ai.common.exception.AiGuardianException;
import com.enterprise.ai.common.validation.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global reactive exception handler delivering standardized RFC 7807 problem details
 * for the AI Gateway with OWASP information leakage prevention.
 */
@RestControllerAdvice
public class GatewayGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayGlobalExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBindException(WebExchangeBindException ex, ServerHttpRequest request) {
        log.warn("WebFlux payload validation failure on URI {}: {}", request.getURI().getPath(), ex.getMessage());

        List<ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                validationErrors.add(ValidationError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Request constraint validation failed")
                .errorCode("VALIDATION_FAILED")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(SecurityException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSecurityException(SecurityException ex, ServerHttpRequest request) {
        log.warn("Security exception on URI {}: {}", request.getURI().getPath(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage() != null ? ex.getMessage() : "Authentication or cryptographic signature verification failed")
                .errorCode("AUTHENTICATION_FAILED")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(AiGuardianException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAiGuardianException(AiGuardianException ex, ServerHttpRequest request) {
        log.error("AI Guardian domain exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode("GATEWAY_PIPELINE_ERROR")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientException(WebClientResponseException ex, ServerHttpRequest request) {
        log.error("Downstream microservice call failed with status={}: {}", ex.getStatusCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Bad Gateway")
                .message("Downstream AI governance microservice returned error: " + ex.getStatusCode())
                .errorCode("DOWNSTREAM_SERVICE_ERROR")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex, ServerHttpRequest request) {
        log.warn("Illegal argument exception on URI {}: {}", request.getURI().getPath(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneralException(Exception ex, ServerHttpRequest request) {
        log.error("Unhandled exception in Gateway on URI {}: {}", request.getURI().getPath(), ex.getMessage(), ex);

        // OWASP Recommendation: Sanitized error response without exposing internal stack traces
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An internal error occurred while processing the gateway request. Ref: " + request.getId())
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}
