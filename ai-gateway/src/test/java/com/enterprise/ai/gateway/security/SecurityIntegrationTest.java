package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.dto.ChatResponse;
import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.config.SecurityWebFilter;
import com.enterprise.ai.gateway.controller.GatewayController;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.service.GatewayPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GatewayController.class)
@Import({SecurityWebFilter.class, RateLimitingService.class, JwtTokenService.class})
@DisplayName("Security & OWASP Defense Integration Tests")
class SecurityIntegrationTest {

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

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        when(circuitBreaker.getState()).thenReturn(LlmCircuitBreaker.State.CLOSED);
        when(llmProperties.isMock()).thenReturn(true);
    }

    @Test
    @DisplayName("OWASP: Responses must contain strict security headers and correlation ID")
    void testSecurityHeadersEnforced() {
        webTestClient.get()
                .uri("/api/v1/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("X-Frame-Options", "DENY")
                .expectHeader().valueEquals("X-XSS-Protection", "0")
                .expectHeader().valueMatches("Strict-Transport-Security", "max-age=.*")
                .expectHeader().valueMatches("Content-Security-Policy", "default-src 'self'.*")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectHeader().valueMatches("Cache-Control", ".*no-store.*")
                .expectHeader().exists("X-Correlation-Id");
    }

    @Test
    @DisplayName("JWT: Valid Bearer token should authenticate and execute chat pipeline")
    void testValidJwtBearerAuthentication() {
        String token = jwtTokenService.generateToken("secops.lead@enterprise.ai", UserRole.ENGINEER, "secops.lead@enterprise.ai");

        ChatResponse mockResponse = ChatResponse.builder()
                .requestId("req-sec-101")
                .allowed(true)
                .action("ALLOW")
                .answer("Authentication verified.")
                .pipelineTrace(Collections.emptyList())
                .build();

        when(pipelineService.processChat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        ChatRequest request = ChatRequest.builder()
                .requestId("req-sec-101")
                .prompt("Summarize compliance protocols")
                .build();

        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true)
                .jsonPath("$.action").isEqualTo("ALLOW");
    }

    @Test
    @DisplayName("Rate Limiting: Exceeding role quota returns 429 with RFC headers")
    void testRateLimitExceededReturns429() {
        // Intern quota is 20 requests per minute
        for (int i = 0; i < 20; i++) {
            webTestClient.post()
                    .uri("/api/v1/ai/chat")
                    .header("X-User-Role", "INTERN")
                    .header("X-Forwarded-For", "192.168.10.55")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ChatRequest.builder().requestId("req-" + i).userId("intern.bob").userRole("INTERN").prompt("Hello").build())
                    .exchange();
        }

        // 21st request must trigger 429 Too Many Requests
        webTestClient.post()
                .uri("/api/v1/ai/chat")
                .header("X-User-Role", "INTERN")
                .header("X-Forwarded-For", "192.168.10.55")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ChatRequest.builder().requestId("req-exceeded").userId("intern.bob").userRole("INTERN").prompt("Hello").build())
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectHeader().exists("Retry-After")
                .expectHeader().valueEquals("X-RateLimit-Remaining", "0")
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("RATE_LIMIT_EXCEEDED")
                .jsonPath("$.status").isEqualTo(429);
    }

    @Test
    @DisplayName("CORS: Preflight OPTIONS request should return permitted origin and headers")
    void testCorsPreflight() {
        webTestClient.options()
                .uri("/api/v1/ai/chat")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type")
                .exchange()
                .expectStatus().isOk();
    }
}
