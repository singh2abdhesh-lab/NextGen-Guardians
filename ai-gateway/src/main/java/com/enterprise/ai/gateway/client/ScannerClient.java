package com.enterprise.ai.gateway.client;

import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.ScanRequest;
import com.enterprise.ai.common.dto.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive client wrapper communicating with the Prompt Security Scanner microservice.
 */
@Component
public class ScannerClient {

    private static final Logger log = LoggerFactory.getLogger(ScannerClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    public ScannerClient(@Qualifier("scannerWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Executes security threat, PII, and injection scan against input prompt.
     *
     * @param requestId correlation request identifier
     * @param prompt    raw input prompt
     * @return Mono of ScanResult
     */
    public Mono<ScanResult> scan(String requestId, String prompt) {
        ScanRequest request = ScanRequest.builder()
                .requestId(requestId)
                .prompt(prompt)
                .build();

        return webClient.post()
                .uri("/api/v1/scan")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ScanResult.class)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Scanner client error for requestId={}: {}", requestId, e.getMessage()));
    }

    /**
     * Executes sensitive token redaction and masking against input prompt.
     *
     * @param requestId correlation request identifier
     * @param prompt    raw input prompt
     * @return Mono of RedactResult
     */
    public Mono<RedactResult> redact(String requestId, String prompt) {
        ScanRequest request = ScanRequest.builder()
                .requestId(requestId)
                .prompt(prompt)
                .build();

        return webClient.post()
                .uri("/api/v1/scan/redact")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RedactResult.class)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Redaction client error for requestId={}: {}", requestId, e.getMessage()));
    }
}

