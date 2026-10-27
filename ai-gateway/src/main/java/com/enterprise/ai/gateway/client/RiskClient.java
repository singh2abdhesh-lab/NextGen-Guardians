package com.enterprise.ai.gateway.client;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive client wrapper communicating with the Risk Scoring Engine microservice.
 */
@Component
public class RiskClient {

    private static final Logger log = LoggerFactory.getLogger(RiskClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    public RiskClient(@Qualifier("riskWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Assesses composite contextual risk across data sensitivity, role, category, and model.
     *
     * @param request risk assessment request payload
     * @return Mono of RiskScore
     */
    public Mono<RiskScore> assess(RiskAssessRequest request) {
        return webClient.post()
                .uri("/api/v1/risk/assess")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RiskScore.class)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Risk client error for requestId={}: {}", request.getRequestId(), e.getMessage()));
    }
}

