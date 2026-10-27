package com.enterprise.ai.gateway.service;

import com.enterprise.ai.common.dto.*;
import com.enterprise.ai.common.dto.ChatResponse.PipelineStage;
import com.enterprise.ai.common.dto.ChatResponse.Usage;
import com.enterprise.ai.gateway.client.LlmClient;
import com.enterprise.ai.gateway.client.QualityClient;
import com.enterprise.ai.gateway.client.RiskClient;
import com.enterprise.ai.gateway.client.ScannerClient;
import com.enterprise.ai.gateway.engine.DecisionEngine;
import com.enterprise.ai.gateway.engine.DecisionEngine.GateDecision;
import com.enterprise.ai.gateway.pipeline.PipelineTracer;
import com.enterprise.ai.gateway.publisher.AuditPublisher;
import com.enterprise.ai.gateway.security.IdentityValidator;
import com.enterprise.ai.gateway.security.IdentityValidator.IdentityValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core reactive orchestrator executing the full AI Guardian governance pipeline.
 */
@Service
public class GatewayPipelineService {

    private static final Logger log = LoggerFactory.getLogger(GatewayPipelineService.class);

    private final IdentityValidator identityValidator;
    private final ScannerClient scannerClient;
    private final RiskClient riskClient;
    private final QualityClient qualityClient;
    private final LlmClient llmClient;
    private final DecisionEngine decisionEngine;
    private final PipelineTracer pipelineTracer;
    private final AuditPublisher auditPublisher;

    public GatewayPipelineService(
            IdentityValidator identityValidator,
            ScannerClient scannerClient,
            RiskClient riskClient,
            QualityClient qualityClient,
            LlmClient llmClient,
            DecisionEngine decisionEngine,
            PipelineTracer pipelineTracer,
            AuditPublisher auditPublisher) {
        this.identityValidator = identityValidator;
        this.scannerClient = scannerClient;
        this.riskClient = riskClient;
        this.qualityClient = qualityClient;
        this.llmClient = llmClient;
        this.decisionEngine = decisionEngine;
        this.pipelineTracer = pipelineTracer;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Executes the reactive chat pipeline end-to-end.
     *
     * @param request the input ChatRequest
     * @return Mono of ChatResponse
     */
    public Mono<ChatResponse> processChat(ChatRequest request) {
        if (request.getRequestId() == null || request.getRequestId().isBlank()) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        String requestId = request.getRequestId();
        List<PipelineStage> trace = pipelineTracer.createTrace();

        // Stage 1: INTERCEPT
        pipelineTracer.recordStage(trace, "INTERCEPT", "ai-gateway", "OK",
                "Request intercepted and correlated", request, Map.of("requestId", requestId));

        // Stage 2: IDENTITY
        IdentityValidationResult identityResult = identityValidator.validate(request);
        if (!identityResult.isAuthorized()) {
            pipelineTracer.recordStage(trace, "IDENTITY", "ai-gateway", "BLOCKED",
                    identityResult.getReason(),
                    Map.of("userId", str(request.getUserId()), "userRole", str(request.getUserRole())),
                    Map.of("authorized", false));

            pipelineTracer.recordSkipped(trace,
                    List.of("SCAN", "REDACT", "RISK", "DECISION", "LLM", "QUALITY", "AUDIT"),
                    "Skipped due to identity authorization failure");

            return Mono.just(buildBlockedResponse(requestId, identityResult.getReason(), null, null, null, trace));
        }

        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            return Mono.just(buildBlockedResponse(requestId, "Prompt payload cannot be empty", null, null, null, trace));
        }

        if (request.getPrompt().length() > 32000) {
            return Mono.just(buildBlockedResponse(requestId, "Prompt exceeds maximum permitted character threshold (max 32,000 characters)", null, null, null, trace));
        }

        // Clean hidden zero-width unicode characters (OWASP Prompt Injection defense)
        request.setPrompt(com.enterprise.ai.common.util.SanitizationUtils.stripInvisibleChars(request.getPrompt()));

        // Propagate authenticated identity claims forward
        request.setUserId(identityResult.getUserId());
        request.setUserRole(identityResult.getUserRole());

        pipelineTracer.recordStage(trace, "IDENTITY", "ai-gateway", "OK",
                "Identity validated: " + identityResult.getUserId() + " (" + identityResult.getUserRole() + ")",
                Map.of("userId", identityResult.getUserId(), "userRole", identityResult.getUserRole()),
                Map.of("authorized", true));

        // Stage 3: SCAN
        return scannerClient.scan(requestId, request.getPrompt())
                .flatMap(scanResult -> {
                    pipelineTracer.recordStage(trace, "SCAN", "prompt-security-scanner",
                            scanResult.isBlocked() ? "BLOCKED" : "OK",
                            "Scan complete: " + scanResult.getViolations().size() + " violations detected, sensitivity=" + scanResult.getSensitivityScore(),
                            Map.of("promptLength", request.getPrompt().length()), scanResult);

                    // Stage 4: REDACT (Auto Redaction)
                    if (scanResult.isBlocked() && request.isAutoRedact()) {
                        return handleAutoRedact(request, requestId, scanResult, trace);
                    } else {
                        pipelineTracer.recordStage(trace, "REDACT", "prompt-security-scanner", "SKIPPED",
                                scanResult.isBlocked() ? "Auto-redaction not requested" : "No redaction required",
                                null, null);
                        return continueWithRiskAssessment(request, requestId, scanResult, request.getPrompt(), false, trace);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Pipeline failure for requestId={}", requestId, e);
                    pipelineTracer.recordStage(trace, "SCAN", "prompt-security-scanner", "FAILED",
                            "Pipeline error: " + e.getMessage(), null, null);
                    return Mono.just(buildErrorResponse(requestId, "Pipeline error: " + e.getMessage(), trace));
                });
    }

    private Mono<ChatResponse> handleAutoRedact(ChatRequest request, String requestId,
                                                ScanResult originalScan, List<PipelineStage> trace) {
        return scannerClient.redact(requestId, request.getPrompt())
                .flatMap(redactResult -> {
                    String redactedPrompt = redactResult.getRedactedPrompt();

                    // Re-scan the redacted prompt to ensure safety
                    return scannerClient.scan(requestId, redactedPrompt)
                            .flatMap(reScanResult -> {
                                if (reScanResult.isBlocked()) {
                                    pipelineTracer.recordStage(trace, "REDACT", "prompt-security-scanner", "BLOCKED",
                                            "Auto-redaction incomplete: redacted prompt still contains security violations",
                                            Map.of("originalLength", request.getPrompt().length()), redactResult);

                                    pipelineTracer.recordSkipped(trace,
                                            List.of("RISK", "DECISION", "LLM", "QUALITY", "AUDIT"),
                                            "Skipped: auto-redaction could not clear all threat vectors");

                                    ChatResponse blocked = buildBlockedResponse(requestId, "Auto-redaction incomplete",
                                            originalScan, null, null, trace);
                                    return publishAuditAndReturn(blocked, request, originalScan, null, null, trace);
                                }

                                pipelineTracer.recordStage(trace, "REDACT", "prompt-security-scanner", "OK",
                                        "Auto-redacted " + redactResult.getViolations().size() + " sensitive tokens",
                                        Map.of("originalLength", request.getPrompt().length()), redactResult);

                                return continueWithRiskAssessment(request, requestId, reScanResult, redactedPrompt, true, trace);
                            });
                });
    }

    private Mono<ChatResponse> continueWithRiskAssessment(ChatRequest request, String requestId,
                                                          ScanResult scanResult, String effectivePrompt,
                                                          boolean redacted, List<PipelineStage> trace) {
        // Stage 5: RISK
        RiskAssessRequest riskReq = RiskAssessRequest.builder()
                .requestId(requestId)
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .promptCategory(request.getPromptCategory())
                .model(request.getModel())
                .dataSensitivity(scanResult.getSensitivityScore())
                .detectedCategories(scanResult.getDetectedCategories())
                .build();

        return riskClient.assess(riskReq)
                .flatMap(riskScore -> {
                    pipelineTracer.recordStage(trace, "RISK", "risk-scoring-engine", "OK",
                            "Risk assessed: totalScore=" + riskScore.getTotalScore() + ", level=" + riskScore.getRiskLevel(),
                            riskReq, riskScore);

                    // Stage 6: DECISION
                    GateDecision preGateDecision = decisionEngine.evaluatePromptGate(scanResult, riskScore);

                    if (!preGateDecision.isAllowed()) {
                        pipelineTracer.recordStage(trace, "DECISION", "ai-gateway", "BLOCKED",
                                preGateDecision.getReason(),
                                Map.of("scanBlocked", scanResult.isBlocked(), "riskAction", riskScore.getAction().name()),
                                Map.of("action", "BLOCKED", "reason", preGateDecision.getReason()));

                        pipelineTracer.recordSkipped(trace, List.of("LLM", "QUALITY"),
                                "LLM execution omitted for blocked request");

                        ChatResponse blocked = buildBlockedResponse(requestId, preGateDecision.getReason(),
                                scanResult, riskScore, null, trace);
                        return publishAuditAndReturn(blocked, request, scanResult, riskScore, null, trace);
                    }

                    pipelineTracer.recordStage(trace, "DECISION", "ai-gateway", preGateDecision.getAction(),
                            "Gate decision: " + preGateDecision.getAction() + " (risk=" + riskScore.getTotalScore() + ")",
                            Map.of("scanBlocked", false, "riskAction", riskScore.getAction().name()),
                            Map.of("action", preGateDecision.getAction()));

                    // Stage 7: LLM
                    return llmClient.complete(requestId, effectivePrompt, request.getModel())
                            .flatMap(llmCompletion -> {
                                pipelineTracer.recordStage(trace, "LLM", "ai-gateway", "OK",
                                        "LLM completion generated: " + llmCompletion.getTokensIn() + " in / " + llmCompletion.getTokensOut() + " out, cost=$" + String.format("%.6f", llmCompletion.getCostUsd()),
                                        Map.of("model", str(request.getModel()), "promptLength", effectivePrompt.length()),
                                        Map.of("tokensIn", llmCompletion.getTokensIn(), "tokensOut", llmCompletion.getTokensOut(), "costUsd", llmCompletion.getCostUsd()));

                                // Stage 8: QUALITY
                                return qualityClient.check(requestId, effectivePrompt, llmCompletion.getAnswer())
                                        .flatMap(qualityResult -> {
                                            GateDecision finalGateDecision = decisionEngine.evaluatePostCompletionGate(preGateDecision, qualityResult);

                                            String qualStatus = qualityResult.getVerdict() == QualityResult.Verdict.FAIL ? "REVIEW" :
                                                    qualityResult.getVerdict() == QualityResult.Verdict.REVIEW ? "REVIEW" : "OK";

                                            pipelineTracer.recordStage(trace, "QUALITY", "response-quality-checker", qualStatus,
                                                    "Quality verified: verdict=" + qualityResult.getVerdict() + ", hallucinationScore=" + qualityResult.getHallucinationScore(),
                                                    Map.of("promptLength", effectivePrompt.length(), "answerLength", llmCompletion.getAnswer().length()),
                                                    qualityResult);

                                            ChatResponse response = ChatResponse.builder()
                                                    .requestId(requestId)
                                                    .allowed(true)
                                                    .action(finalGateDecision.getAction())
                                                    .answer(llmCompletion.getAnswer())
                                                    .scan(scanResult)
                                                    .risk(riskScore)
                                                    .quality(qualityResult)
                                                    .usage(Usage.builder()
                                                            .tokensIn(llmCompletion.getTokensIn())
                                                            .tokensOut(llmCompletion.getTokensOut())
                                                            .costUsd(llmCompletion.getCostUsd())
                                                            .build())
                                                    .approvalRequired(finalGateDecision.isApprovalRequired())
                                                    .pipelineTrace(trace)
                                                    .redacted(redacted)
                                                    .effectivePrompt(redacted ? effectivePrompt : null)
                                                    .build();

                                            return publishAuditAndReturn(response, request, scanResult, riskScore, qualityResult, trace);
                                        });
                            });
                });
    }

    private Mono<ChatResponse> publishAuditAndReturn(ChatResponse response, ChatRequest request,
                                                     ScanResult scan, RiskScore risk,
                                                     QualityResult quality, List<PipelineStage> trace) {
        String violationsSummary = "";
        if (scan != null && scan.getViolations() != null && !scan.getViolations().isEmpty()) {
            violationsSummary = scan.getViolations().stream()
                    .map(v -> v.getType() + "(" + v.getSeverity() + ")")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        AuditEvent event = AuditEvent.builder()
                .requestId(response.getRequestId())
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .model(request.getModel())
                .promptCategory(request.getPromptCategory())
                .action(response.getAction())
                .riskScore(risk != null ? risk.getTotalScore() : 0)
                .riskLevel(risk != null ? risk.getRiskLevel().name() : null)
                .tokensIn(response.getUsage() != null ? response.getUsage().getTokensIn() : 0)
                .tokensOut(response.getUsage() != null ? response.getUsage().getTokensOut() : 0)
                .costUsd(response.getUsage() != null ? response.getUsage().getCostUsd() : 0.0)
                .qualityVerdict(quality != null ? quality.getVerdict().name() : null)
                .hallucinationScore(quality != null ? quality.getHallucinationScore() : null)
                .approvalRequired(response.isApprovalRequired())
                .violationsSummary(violationsSummary)
                .blockReason(response.getBlockReason())
                .prompt(request.getPrompt())
                .timestamp(LocalDateTime.now())
                .build();

        // Stage 9: AUDIT
        pipelineTracer.recordStage(trace, "AUDIT", "audit-service", "OK",
                "Audit event recorded (asynchronous background delivery)",
                event, Map.of("published", true));

        // Fire-and-forget publication
        auditPublisher.publish(event).subscribe();

        return Mono.just(response);
    }

    private ChatResponse buildBlockedResponse(String requestId, String blockReason,
                                              ScanResult scan, RiskScore risk,
                                              QualityResult quality, List<PipelineStage> trace) {
        return ChatResponse.builder()
                .requestId(requestId)
                .allowed(false)
                .action("BLOCKED")
                .blockReason(blockReason)
                .scan(scan)
                .risk(risk)
                .quality(quality)
                .usage(Usage.builder().tokensIn(0).tokensOut(0).costUsd(0.0).build())
                .approvalRequired(false)
                .pipelineTrace(trace)
                .redacted(false)
                .build();
    }

    private ChatResponse buildErrorResponse(String requestId, String error, List<PipelineStage> trace) {
        return ChatResponse.builder()
                .requestId(requestId)
                .allowed(false)
                .action("BLOCKED")
                .blockReason(error)
                .usage(Usage.builder().tokensIn(0).tokensOut(0).costUsd(0.0).build())
                .pipelineTrace(trace)
                .build();
    }

    private String str(String s) {
        return s != null ? s : "";
    }
}
