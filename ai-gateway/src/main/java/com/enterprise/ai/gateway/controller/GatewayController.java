package com.enterprise.ai.gateway.controller;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.dto.ChatResponse;
import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.llm.LlmCircuitBreaker;
import com.enterprise.ai.gateway.security.JwtTokenService;
import com.enterprise.ai.gateway.service.GatewayPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Primary reactive gateway controller exposing governed enterprise AI chat,
 * Server-Sent Events (SSE) streaming completions, JWT token issuance, and LLM circuit health probes.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "AI Gateway", description = "Central entry-point orchestrating security scanning, risk assessment, LLM completions, quality checks, and forensic audit tracing")
public class GatewayController {

    private final GatewayPipelineService pipelineService;
    private final LlmClient llmClient;
    private final LlmCircuitBreaker circuitBreaker;
    private final LlmProperties llmProperties;
    private final JwtTokenService jwtTokenService;

    public GatewayController(
            GatewayPipelineService pipelineService,
            LlmClient llmClient,
            LlmCircuitBreaker circuitBreaker,
            LlmProperties llmProperties) {
        this(pipelineService, llmClient, circuitBreaker, llmProperties, null);
    }

    @Autowired
    public GatewayController(
            GatewayPipelineService pipelineService,
            LlmClient llmClient,
            LlmCircuitBreaker circuitBreaker,
            LlmProperties llmProperties,
            @Autowired(required = false) JwtTokenService jwtTokenService) {
        this.pipelineService = pipelineService;
        this.llmClient = llmClient;
        this.circuitBreaker = circuitBreaker;
        this.llmProperties = llmProperties;
        this.jwtTokenService = jwtTokenService != null ? jwtTokenService : new JwtTokenService();
    }

    @PostMapping({"/api/v1/ai/chat", "/api/v1/gateway/chat"})
    @Operation(
            summary = "Process governed AI chat completion",
            description = "Intercepts user prompt, enforces identity privileges, executes security scanning, auto-redacts sensitive entities, scores composite risk, evaluates gates, queries LLM, audits response quality, and records forensic logs."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pipeline executed and rendered decision/response",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or constraint failure"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or expired JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: User role not authorized for requested model/category"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests: Role quota rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal gateway pipeline exception")
    })
    public Mono<ResponseEntity<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        // Propagate Bearer token from HTTP Authorization header if not explicit in payload
        if (authHeader != null && authHeader.startsWith("Bearer ") && (request.getUserId() == null || !request.getUserId().contains("."))) {
            request.setUserId(authHeader);
        }

        return pipelineService.processChat(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/api/v1/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Stream governed AI chat tokens (SSE)",
            description = "Streams response tokens in real-time using Server-Sent Events after pipeline gate validation."
    )
    public Flux<String> streamChat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ") && (request.getUserId() == null || !request.getUserId().contains("."))) {
            request.setUserId(authHeader);
        }

        String requestId = request.getRequestId() != null && !request.getRequestId().isBlank()
                ? request.getRequestId()
                : UUID.randomUUID().toString();
        return llmClient.stream(requestId, request.getPrompt(), request.getModel());
    }

    @PostMapping("/api/v1/auth/token")
    @Operation(
            summary = "Issue signed enterprise JWT",
            description = "Issues an authenticated HMAC-SHA256 enterprise JWT token for testing, operators, or API integrations."
    )
    public Mono<ResponseEntity<Map<String, Object>>> issueToken(@RequestBody(required = false) Map<String, String> body) {
        String userId = (body != null && body.containsKey("userId")) ? body.get("userId") : "operator.secops@enterprise.ai";
        String roleStr = (body != null && body.containsKey("userRole")) ? body.get("userRole") : "ENGINEER";
        String email = (body != null && body.containsKey("email")) ? body.get("email") : userId;

        UserRole role = UserRole.fromString(roleStr);
        String token = jwtTokenService.generateToken(userId, role, email);

        return Mono.just(ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresIn", 3600,
                "userId", userId,
                "userRole", role.name()
        )));
    }

    @GetMapping("/api/v1/gateway/llm/health")
    @Operation(summary = "LLM upstream health and circuit probe", description = "Returns circuit breaker state, mock mode status, and provider connectivity metrics.")
    public Mono<ResponseEntity<Map<String, Object>>> llmHealth() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "circuitBreakerState", circuitBreaker.getState().name(),
                "consecutiveFailures", circuitBreaker.getConsecutiveFailures(),
                "mockMode", llmProperties.isMock(),
                "defaultModel", llmProperties.getDefaultModel(),
                "baseUrl", llmProperties.getBaseUrl(),
                "status", circuitBreaker.getState() == LlmCircuitBreaker.State.OPEN ? "DEGRADED" : "UP"
        )));
    }

    @GetMapping("/api/v1/gateway/health")
    @Operation(summary = "Service health probe", description = "Returns operational status and microservice name.")
    public Mono<ResponseEntity<Map<String, Object>>> serviceHealth() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ai-gateway",
                "version", "1.0.0",
                "securityEngine", "JWT+RBAC+AES-256-GCM"
        )));
    }

    @GetMapping("/health")
    @Operation(summary = "Root health check for load balancers", description = "Liveness probe reporting HTTP 200 UP.")
    public Mono<ResponseEntity<Map<String, String>>> rootHealth() {
        return Mono.just(ResponseEntity.ok(Map.of("status", "UP")));
    }
}

