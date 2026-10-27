package com.enterprise.ai.gateway.client;

import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.llm.CostCalculator;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.llm.TokenCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LLM Client Unit Tests")
class LlmClientTest {

    @Mock
    private WebClient webClient;

    private LlmProperties properties;
    private TokenCalculator tokenCalculator;
    private CostCalculator costCalculator;
    private LlmCircuitBreaker circuitBreaker;
    private LlmClient llmClient;

    @BeforeEach
    void setUp() {
        properties = new LlmProperties();
        properties.setMock(true); // Test mock mode first
        tokenCalculator = new TokenCalculator();
        costCalculator = new CostCalculator(properties);
        circuitBreaker = new LlmCircuitBreaker(properties);

        llmClient = new LlmClient(webClient, properties, tokenCalculator, costCalculator, circuitBreaker);
    }

    @Test
    @DisplayName("Mock mode completion returns governed response with tokens and cost")
    void testMockCompletion() {
        StepVerifier.create(llmClient.complete("req-mock-1", "Explain microservices", "gpt-4o"))
                .assertNext(completion -> {
                    assertNotNull(completion);
                    assertTrue(completion.getAnswer().contains("governed enterprise AI response"));
                    assertTrue(completion.getTokensIn() > 0);
                    assertTrue(completion.getTokensOut() > 0);
                    assertTrue(completion.getCostUsd() > 0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Streaming yields word tokens as reactive Flux")
    void testStreaming() {
        StepVerifier.create(llmClient.stream("req-stream-1", "Hello AI", "gpt-4o").take(3))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    @DisplayName("Open circuit breaker returns fallback completion without network call")
    void testCircuitBreakerFallback() {
        properties.setMock(false);
        // Trip circuit breaker
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();

        StepVerifier.create(llmClient.complete("req-cb-1", "Test prompt", "gpt-4o"))
                .assertNext(completion -> {
                    assertNotNull(completion);
                    assertTrue(completion.getAnswer().contains("[CIRCUIT_BREAKER_FALLBACK]"));
                })
                .verifyComplete();
    }
}
