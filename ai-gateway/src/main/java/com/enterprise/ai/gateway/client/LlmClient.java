package com.enterprise.ai.gateway.client;

import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.llm.CostCalculator;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.llm.TokenCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Enterprise reactive client communicating with upstream Large Language Models,
 * featuring OpenAI-compatible API protocol, governed Mock Mode, token accounting,
 * cost calculations, exponential retry, timeouts, streaming, and circuit breaker resilience.
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final WebClient webClient;
    private final LlmProperties properties;
    private final TokenCalculator tokenCalculator;
    private final CostCalculator costCalculator;
    private final LlmCircuitBreaker circuitBreaker;

    public LlmClient(
            @Qualifier("llmWebClient") WebClient webClient,
            LlmProperties properties,
            TokenCalculator tokenCalculator,
            CostCalculator costCalculator,
            LlmCircuitBreaker circuitBreaker) {
        this.webClient = webClient;
        this.properties = properties;
        this.tokenCalculator = tokenCalculator;
        this.costCalculator = costCalculator;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Executes non-streaming completion against upstream LLM with retries, timeouts,
     * and circuit breaker fallback.
     *
     * @param requestId correlation request identifier
     * @param prompt    prompt string to dispatch
     * @param model     target model identifier (e.g. gpt-4o)
     */
    public Mono<LlmCompletion> complete(com.enterprise.ai.common.dto.ChatRequest request) {
        if (request == null) return Mono.empty();
        return complete(request.getRequestId(), request.getPrompt(), request.getModel());
    }

    public Mono<LlmCompletion> complete(Object request) {
        if (request instanceof com.enterprise.ai.common.dto.ChatRequest chatReq) {
            return complete(chatReq);
        }
        return complete("mock-req", request != null ? request.toString() : "", properties.getDefaultModel());
    }

    public Mono<LlmCompletion> complete(String requestId, String prompt, String model) {
        String effectiveModel = (model != null && !model.isBlank()) ? model : properties.getDefaultModel();

        // 1. Mock Mode check
        if (properties.isMock()) {
            return generateMockCompletion(prompt, effectiveModel);
        }

        // 2. Circuit Breaker check
        if (!circuitBreaker.allowExecution()) {
            log.warn("Circuit Breaker is OPEN for upstream LLM; falling back to governed mock response for requestId={}", requestId);
            return generateMockCompletion(prompt, effectiveModel)
                    .map(c -> new LlmCompletion(
                            "[CIRCUIT_BREAKER_FALLBACK] " + c.getAnswer(),
                            c.getTokensIn(),
                            c.getTokensOut(),
                            0.0
                    ));
        }

        // 3. OpenAI Chat Completion payload
        Map<String, Object> body = Map.of(
                "model", effectiveModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 1000,
                "temperature", 0.7
        );

        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());

        return webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(properties.getMaxRetries(), Duration.ofMillis(properties.getRetryBackoffMs()))
                        .filter(this::isTransientError)
                        .doBeforeRetry(retrySignal -> log.warn("Retrying LLM call (attempt {}) for requestId={}: {}",
                                retrySignal.totalRetries() + 1, requestId, retrySignal.failure().getMessage())))
                .map(resp -> {
                    circuitBreaker.recordSuccess();
                    return parseLlmResponse(resp, effectiveModel, prompt);
                })
                .onErrorResume(e -> {
                    circuitBreaker.recordFailure();
                    log.error("LLM upstream call failed for requestId={}: {}", requestId, e.getMessage());
                    // Graceful fallback to mock response
                    return generateMockCompletion(prompt, effectiveModel)
                            .map(c -> new LlmCompletion(
                                    "[DEGRADED_FALLBACK] " + c.getAnswer(),
                                    c.getTokensIn(),
                                    c.getTokensOut(),
                                    0.0
                            ));
                });
    }

    /**
     * Executes streaming completion yielding token/word chunks via reactive Flux.
     *
     * @param requestId correlation request identifier
     * @param prompt    prompt string to dispatch
     * @param model     target model identifier
     * @return Flux of String tokens/chunks
     */
    public Flux<String> stream(String requestId, String prompt, String model) {
        String effectiveModel = (model != null && !model.isBlank()) ? model : properties.getDefaultModel();

        if (properties.isMock() || !circuitBreaker.allowExecution()) {
            return generateMockStream(prompt, effectiveModel);
        }

        Map<String, Object> body = Map.of(
                "model", effectiveModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 1000,
                "stream", true
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .filter(chunk -> chunk != null && !chunk.isBlank())
                .onErrorResume(e -> {
                    log.warn("Streaming failure for requestId={}; falling back to mock stream: {}", requestId, e.getMessage());
                    return generateMockStream(prompt, effectiveModel);
                });
    }

    private Mono<LlmCompletion> generateMockCompletion(String prompt, String model) {
        return Mono.fromCallable(() -> {
            String answer = "This is a governed enterprise AI response generated for model '" + model + "'. " +
                    "The prompt was verified through Enterprise AI Guardian security, privacy, and quality controls.";

            int tokensIn = tokenCalculator.calculateMessageTokens("user", prompt);
            int tokensOut = tokenCalculator.calculateTokens(answer);
            double cost = costCalculator.calculateCost(model, tokensIn, tokensOut);

            return new LlmCompletion(answer, tokensIn, tokensOut, cost);
        });
    }

    private Flux<String> generateMockStream(String prompt, String model) {
        String response = "This is a streaming enterprise AI response for model '" + model + "'. " +
                "All prompt inputs and streaming completion chunks are governed by the AI Guardian platform.";

        String[] words = response.split(" ");
        return Flux.fromArray(words)
                .map(word -> word + " ")
                .delayElements(Duration.ofMillis(30));
    }

    @SuppressWarnings("unchecked")
    private LlmCompletion parseLlmResponse(Map<?, ?> resp, String model, String originalPrompt) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        String answer = "";
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message != null && message.get("content") != null) {
                answer = message.get("content").toString();
            }
        }

        int tokensIn;
        int tokensOut;

        Map<String, Object> usage = (Map<String, Object>) resp.get("usage");
        if (usage != null && usage.get("prompt_tokens") != null && usage.get("completion_tokens") != null) {
            tokensIn = ((Number) usage.get("prompt_tokens")).intValue();
            tokensOut = ((Number) usage.get("completion_tokens")).intValue();
        } else {
            tokensIn = tokenCalculator.calculateMessageTokens("user", originalPrompt);
            tokensOut = tokenCalculator.calculateTokens(answer);
        }

        double cost = costCalculator.calculateCost(model, tokensIn, tokensOut);
        return new LlmCompletion(answer, tokensIn, tokensOut, cost);
    }

    private boolean isTransientError(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            return true;
        }
        if (throwable instanceof WebClientRequestException) {
            return true; // Network/connectivity errors
        }
        if (throwable instanceof WebClientResponseException wce) {
            int status = wce.getStatusCode().value();
            return status == 429 || (status >= 500 && status < 600); // Rate limits and upstream server errors
        }
        return false;
    }

    public static class LlmCompletion {
        private final String answer;
        private final int tokensIn;
        private final int tokensOut;
        private final double costUsd;

        public LlmCompletion(String answer, int tokensIn, int tokensOut, double costUsd) {
            this.answer = answer;
            this.tokensIn = tokensIn;
            this.tokensOut = tokensOut;
            this.costUsd = costUsd;
        }

        public String getAnswer() {
            return answer;
        }

        public int getTokensIn() {
            return tokensIn;
        }

        public int getTokensOut() {
            return tokensOut;
        }

        public double getCostUsd() {
            return costUsd;
        }
    }
}

