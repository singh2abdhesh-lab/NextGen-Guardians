package com.enterprise.ai.gateway.client;

import com.enterprise.ai.common.dto.QualityCheckRequest;
import com.enterprise.ai.common.dto.QualityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive client wrapper communicating with the Response Quality Checker microservice.
 */
@Component
public class QualityClient {

    private static final Logger log = LoggerFactory.getLogger(QualityClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    public QualityClient(@Qualifier("qualityWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Executes post-completion hallucination, secret echo, and policy audit.
     *
     * @param requestId correlation request identifier
     * @param prompt    input prompt provided to model
     * @param response  raw model completion
     * @return Mono of QualityResult
     */
    public Mono<QualityResult> check(String requestId, String prompt, String response) {
        QualityCheckRequest request = QualityCheckRequest.builder()
                .requestId(requestId)
                .prompt(prompt)
                .response(response)
                .build();

        return webClient.post()
                .uri("/api/v1/quality/check")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QualityResult.class)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Quality client error for requestId={}: {}", requestId, e.getMessage()));
    }

    public Mono<QualityResult> checkQuality(QualityCheckRequest request) {
        if (request == null) return Mono.empty();
        return check(request.getRequestId(), request.getPrompt(), request.getResponse());
    }
}

