package com.enterprise.ai.gateway.service;

import com.enterprise.ai.common.dto.*;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.client.LlmClient.LlmCompletion;
import com.enterprise.ai.gateway.client.QualityClient;
import com.enterprise.ai.gateway.client.RiskClient;
import com.enterprise.ai.gateway.client.ScannerClient;
import com.enterprise.ai.gateway.engine.DecisionEngine;
import com.enterprise.ai.gateway.pipeline.PipelineTracer;
import com.enterprise.ai.gateway.publisher.AuditPublisher;
import com.enterprise.ai.gateway.security.IdentityValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gateway Pipeline Service Unit Tests")
class GatewayPipelineServiceTest {

    @Mock
    private ScannerClient scannerClient;

    @Mock
    private RiskClient riskClient;

    @Mock
    private QualityClient qualityClient;

    @Mock
    private LlmClient llmClient;

    @Mock
    private AuditPublisher auditPublisher;

    private IdentityValidator identityValidator;
    private DecisionEngine decisionEngine;
    private PipelineTracer pipelineTracer;
    private GatewayPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        identityValidator = new IdentityValidator();
        decisionEngine = new DecisionEngine();
        pipelineTracer = new PipelineTracer(new ObjectMapper());

        pipelineService = new GatewayPipelineService(
                identityValidator,
                scannerClient,
                riskClient,
                qualityClient,
                llmClient,
                decisionEngine,
                pipelineTracer,
                auditPublisher
        );

        lenient().when(auditPublisher.publish(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Authorized clean prompt should traverse entire pipeline and return completion")
    void testFullPipelineSuccess() {
        ChatRequest request = ChatRequest.builder()
                .requestId("req-pipe-1")
                .userId("engineer.alex@enterprise.ai")
                .userRole("ENGINEER")
                .promptCategory("CODE")
                .model("gpt-4o")
                .prompt("Explain Spring WebFlux event loop.")
                .autoRedact(false)
                .build();

        ScanResult scanResult = ScanResult.builder()
                .requestId("req-pipe-1")
                .blocked(false)
                .sensitivityScore(10)
                .violations(Collections.emptyList())
                .build();

        RiskScore riskScore = RiskScore.builder()
                .requestId("req-pipe-1")
                .totalScore(15)
                .riskLevel(RiskLevel.LOW)
                .action(Action.ALLOW)
                .build();

        LlmCompletion completion = new LlmCompletion(
                "Spring WebFlux uses Netty's event loop to achieve non-blocking I/O.",
                20, 25, 0.0003
        );

        QualityResult qualityResult = QualityResult.builder()
                .requestId("req-pipe-1")
                .verdict(Verdict.PASS)
                .hallucinationScore(0.0)
                .policyViolation(false)
                .policyViolations(Collections.emptyList())
                .approvalRequired(false)
                .build();

        when(scannerClient.scan(eq("req-pipe-1"), anyString())).thenReturn(Mono.just(scanResult));
        when(riskClient.assess(any())).thenReturn(Mono.just(riskScore));
        when(llmClient.complete(eq("req-pipe-1"), anyString(), anyString())).thenReturn(Mono.just(completion));
        when(qualityClient.check(eq("req-pipe-1"), anyString(), anyString())).thenReturn(Mono.just(qualityResult));

        StepVerifier.create(pipelineService.processChat(request))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertTrue(response.isAllowed());
                    assertEquals("ALLOWED", response.getAction());
                    assertEquals("Spring WebFlux uses Netty's event loop to achieve non-blocking I/O.", response.getAnswer());
                    assertNotNull(response.getUsage());
                    assertEquals(20, response.getUsage().getTokensIn());
                    assertFalse(response.isApprovalRequired());
                    assertNotNull(response.getPipelineTrace());
                    assertEquals(9, response.getPipelineTrace().size()); // 9 stages
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Invalid identity should immediately halt pipeline and return BLOCKED")
    void testIdentityRejection() {
        ChatRequest request = ChatRequest.builder()
                .requestId("req-unauth-1")
                .userId("") // invalid
                .userRole("ENGINEER")
                .prompt("Hello")
                .build();

        StepVerifier.create(pipelineService.processChat(request))
                .assertNext(response -> {
                    assertFalse(response.isAllowed());
                    assertEquals("BLOCKED", response.getAction());
                    assertTrue(response.getBlockReason().contains("userId is required"));
                    verify(scannerClient, never()).scan(any(), any());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Prompt with autoRedact=true should redact sensitive data and proceed")
    void testAutoRedactionPath() {
        ChatRequest request = ChatRequest.builder()
                .requestId("req-redact-1")
                .userId("analyst@corp.com")
                .userRole("ANALYST")
                .prompt("My SSN is 123-45-6789, please help.")
                .autoRedact(true)
                .build();

        ScanResult initialScan = ScanResult.builder()
                .requestId("req-redact-1")
                .blocked(true)
                .sensitivityScore(80)
                .violations(List.of(Violation.builder().type(com.enterprise.ai.common.enums.ViolationType.PII).severity(Violation.Severity.HIGH).build()))
                .build();

        RedactResult redactResult = RedactResult.builder()
                .requestId("req-redact-1")
                .redactedPrompt("My SSN is [REDACTED_SSN], please help.")
                .violations(List.of(Violation.builder().type(com.enterprise.ai.common.enums.ViolationType.PII).build()))
                .build();

        ScanResult reScan = ScanResult.builder()
                .requestId("req-redact-1")
                .blocked(false)
                .sensitivityScore(10)
                .violations(Collections.emptyList())
                .build();

        RiskScore riskScore = RiskScore.builder()
                .totalScore(20)
                .riskLevel(RiskLevel.LOW)
                .action(Action.ALLOW)
                .build();

        LlmCompletion completion = new LlmCompletion("I can assist with your inquiry.", 10, 10, 0.0001);

        QualityResult qualityResult = QualityResult.builder()
                .verdict(Verdict.PASS)
                .hallucinationScore(0.0)
                .approvalRequired(false)
                .build();

        when(scannerClient.scan("req-redact-1", "My SSN is 123-45-6789, please help.")).thenReturn(Mono.just(initialScan));
        when(scannerClient.redact("req-redact-1", "My SSN is 123-45-6789, please help.")).thenReturn(Mono.just(redactResult));
        when(scannerClient.scan("req-redact-1", "My SSN is [REDACTED_SSN], please help.")).thenReturn(Mono.just(reScan));
        when(riskClient.assess(any())).thenReturn(Mono.just(riskScore));
        when(llmClient.complete(eq("req-redact-1"), eq("My SSN is [REDACTED_SSN], please help."), any())).thenReturn(Mono.just(completion));
        when(qualityClient.check(eq("req-redact-1"), any(), any())).thenReturn(Mono.just(qualityResult));

        StepVerifier.create(pipelineService.processChat(request))
                .assertNext(response -> {
                    assertTrue(response.isAllowed());
                    assertTrue(response.isRedacted());
                    assertEquals("My SSN is [REDACTED_SSN], please help.", response.getEffectivePrompt());
                })
                .verifyComplete();
    }
}
