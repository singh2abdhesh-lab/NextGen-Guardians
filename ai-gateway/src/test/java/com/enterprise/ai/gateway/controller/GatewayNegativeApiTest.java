package com.enterprise.ai.gateway.controller;

import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.config.SecurityWebFilter;
import com.enterprise.ai.gateway.exception.GatewayGlobalExceptionHandler;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.security.JwtTokenService;
import com.enterprise.ai.gateway.security.RateLimitingService;
import com.enterprise.ai.gateway.service.GatewayPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GatewayController.class)
@Import({GatewayGlobalExceptionHandler.class, SecurityWebFilter.class, RateLimitingService.class, JwtTokenService.class})
@DisplayName("Gateway Negative & Constraint API Tests")
class GatewayNegativeApiTest {

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

    @BeforeEach
    void setUp() {
        when(circuitBreaker.getState()).thenReturn(LlmCircuitBreaker.State.CLOSED);
        when(llmProperties.isMock()).thenReturn(true);
    }

    @Test
    @DisplayName("Negative: Malformed JSON syntax should return 400 with RFC 7807 ErrorResponse")
    void testMalformedJsonPayloadReturns400() {
        String malformedJson = "{ \"prompt\": \"Hello\", \"userId\": ";

        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(malformedJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.path").isEqualTo("/api/v1/ai/chat");
    }

    @Test
    @DisplayName("Negative: Blank prompt string should fail bean validation and return 400 with field errors")
    void testEmptyPromptPayloadReturnsValidationErrors() {
        String invalidPayload = "{ \"prompt\": \"   \", \"userId\": \"tester\", \"userRole\": \"ENGINEER\" }";

        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidPayload)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_FAILED")
                .jsonPath("$.validationErrors").isArray();
    }

    @Test
    @DisplayName("Negative: Unsupported HTTP method (DELETE on /api/v1/ai/chat) should return 405 Method Not Allowed")
    void testUnsupportedMethodReturns405() {
        webTestClient.delete()
                .uri("/api/v1/ai/chat")
                .exchange()
                .expectStatus().isEqualTo(405);
    }

    @Test
    @DisplayName("Negative: Unsupported Content-Type (text/plain) should return 415 Unsupported Media Type")
    void testUnsupportedContentTypeReturns415() {
        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Plain text prompt payload")
                .exchange()
                .expectStatus().isEqualTo(415);
    }

    @Test
    @DisplayName("Negative: Downstream microservice 500 error should translate to 502 Bad Gateway without leaking stack traces")
    void testDownstreamErrorTranslatesTo502() {
        when(pipelineService.processChat(any())).thenReturn(
                Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null))
        );

        String validPayload = "{ \"prompt\": \"Valid prompt text\", \"userId\": \"tester\", \"userRole\": \"ENGINEER\" }";

        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validPayload)
                .exchange()
                .expectStatus().isEqualTo(502)
                .expectBody()
                .jsonPath("$.status").isEqualTo(502)
                .jsonPath("$.error").isEqualTo("Bad Gateway")
                .jsonPath("$.errorCode").isEqualTo("DOWNSTREAM_SERVICE_ERROR");
    }
}

