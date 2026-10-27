package com.enterprise.ai.gateway.controller;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.dto.ChatResponse;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.service.GatewayPipelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GatewayController.class)
@DisplayName("Gateway Controller WebFlux Tests")
class GatewayControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GatewayPipelineService pipelineService;

    @MockBean
    private LlmClient llmClient;

    @MockBean
    private LlmCircuitBreaker circuitBreaker;

    @MockBean
    private LlmProperties llmProperties;

    @Test
    @DisplayName("POST /api/v1/ai/chat should return 200 with ChatResponse")
    void testChatEndpointSuccess() {
        ChatResponse mockResponse = ChatResponse.builder()
                .requestId("req-test-1")
                .allowed(true)
                .action("ALLOWED")
                .answer("This is a verified AI response.")
                .pipelineTrace(Collections.emptyList())
                .build();

        when(pipelineService.processChat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        ChatRequest request = ChatRequest.builder()
                .requestId("req-test-1")
                .userId("tester")
                .userRole("ENGINEER")
                .prompt("Hello AI")
                .build();

        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.requestId").isEqualTo("req-test-1")
                .jsonPath("$.allowed").isEqualTo(true)
                .jsonPath("$.action").isEqualTo("ALLOWED")
                .jsonPath("$.answer").isEqualTo("This is a verified AI response.");
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat/stream should stream response tokens via SSE")
    void testStreamingEndpoint() {
        when(llmClient.stream(any(), any(), any())).thenReturn(Flux.just("Hello ", "world!"));

        ChatRequest request = ChatRequest.builder()
                .requestId("req-stream-test")
                .userId("tester")
                .userRole("ENGINEER")
                .prompt("Hello AI")
                .build();

        webTestClient.post()
                .uri("/api/v1/ai/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBody(String.class)
                .value(body -> org.junit.jupiter.api.Assertions.assertTrue(body.contains("Hello ") || body.contains("data:")));
    }

    @Test
    @DisplayName("GET /api/v1/gateway/llm/health returns circuit status and provider metadata")
    void testLlmHealth() {
        when(circuitBreaker.getState()).thenReturn(LlmCircuitBreaker.State.CLOSED);
        when(circuitBreaker.getConsecutiveFailures()).thenReturn(0);
        when(llmProperties.isMock()).thenReturn(true);
        when(llmProperties.getDefaultModel()).thenReturn("gpt-4o");
        when(llmProperties.getBaseUrl()).thenReturn("https://api.openai.com");

        webTestClient.get()
                .uri("/api/v1/gateway/llm/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.circuitBreakerState").isEqualTo("CLOSED")
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.mockMode").isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/v1/gateway/health should return UP status")
    void testGatewayHealth() {
        when(circuitBreaker.getState()).thenReturn(LlmCircuitBreaker.State.CLOSED);
        when(llmProperties.isMock()).thenReturn(true);

        webTestClient.get()
                .uri("/api/v1/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("ai-gateway");
    }

    @Test
    @DisplayName("GET /health root probe should return UP status")
    void testRootHealth() {
        when(circuitBreaker.getState()).thenReturn(LlmCircuitBreaker.State.CLOSED);
        when(llmProperties.isMock()).thenReturn(true);

        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("POST /api/v1/auth/token should issue signed enterprise JWT")
    void testIssueTokenEndpoint() {
        webTestClient.post()
                .uri("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of("userId", "tester@corp.com", "userRole", "ENGINEER"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.userId").isEqualTo("tester@corp.com")
                .jsonPath("$.userRole").isEqualTo("ENGINEER");
    }
}
