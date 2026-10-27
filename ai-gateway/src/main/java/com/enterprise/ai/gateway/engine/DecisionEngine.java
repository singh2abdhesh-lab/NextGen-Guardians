package com.enterprise.ai.gateway.engine;

import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Authoritative pipeline decision engine that evaluates security scan verdicts,
 * composite risk scores, and response quality metrics to enforce policy actions.
 */
@Component
public class DecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(DecisionEngine.class);

    /**
     * Evaluates prompt scan and pre-execution risk to decide whether LLM invocation is permitted.
     *
     * @param scanResult prompt security scan results
     * @param riskScore  calculated multi-factor risk score
     * @return GateDecision specifying allowed, action, blockReason, and approvalRequired
     */
    public GateDecision evaluatePromptGate(ScanResult scanResult, RiskScore riskScore) {
        boolean scanBlocked = scanResult != null && scanResult.isBlocked();
        boolean riskBlocked = riskScore != null && riskScore.getAction() == RiskScore.Action.BLOCK;
        boolean riskReview = riskScore != null && riskScore.getAction() == RiskScore.Action.REVIEW;

        if (scanBlocked || riskBlocked) {
            String blockReason = buildBlockReason(scanResult, riskScore);
            log.warn("Prompt rejected by Decision Engine: reason='{}'", blockReason);
            return GateDecision.blocked(blockReason);
        }

        if (riskReview) {
            log.info("Prompt authorized for LLM execution with mandatory REVIEW required");
            return GateDecision.review(true, "Risk level elevated to REVIEW threshold");
        }

        log.debug("Prompt fully cleared Decision Engine gate with ALLOW status");
        return GateDecision.allowed();
    }

    /**
     * Evaluates model completion quality and synthesizes final response verdict.
     *
     * @param preGateDecision initial decision prior to LLM call
     * @param qualityResult   post-execution quality verification output
     * @return final GateDecision for client response
     */
    public GateDecision evaluatePostCompletionGate(GateDecision preGateDecision, QualityResult qualityResult) {
        if (preGateDecision != null && !preGateDecision.isAllowed()) {
            return preGateDecision;
        }

        boolean qualityBreach = qualityResult != null && qualityResult.isPolicyViolation();
        boolean qualityFail = qualityResult != null && qualityResult.getVerdict() == QualityResult.Verdict.FAIL;
        boolean qualityReview = qualityResult != null && (qualityResult.getVerdict() == QualityResult.Verdict.REVIEW || qualityResult.isApprovalRequired());

        if (qualityBreach || qualityFail) {
            String reason = "Response violated enterprise safety policies or exceeded hallucination threshold";
            if (qualityResult != null && qualityResult.getPolicyViolations() != null && !qualityResult.getPolicyViolations().isEmpty()) {
                reason = String.join("; ", qualityResult.getPolicyViolations());
            }
            log.warn("Model completion flagged by Quality Check: {}", reason);
            return GateDecision.review(false, reason);
        }

        if (qualityReview || (preGateDecision != null && preGateDecision.isApprovalRequired())) {
            log.info("Response marked for human-in-the-loop review");
            return GateDecision.review(false, "Human verification mandated by governance policies");
        }

        return GateDecision.allowed();
    }

    private String buildBlockReason(ScanResult scan, RiskScore risk) {
        StringBuilder sb = new StringBuilder();
        if (scan != null && scan.isBlocked() && scan.getViolations() != null) {
            sb.append("Security scan violations: ");
            scan.getViolations().forEach(v ->
                    sb.append(v.getType()).append("(").append(v.getSeverity()).append(") "));
        }
        if (risk != null && risk.getAction() == RiskScore.Action.BLOCK) {
            if (sb.length() > 0) sb.append("| ");
            sb.append("Risk score ").append(risk.getTotalScore()).append(" (").append(risk.getRiskLevel()).append(") exceeds enterprise threshold");
        }
        return sb.toString().trim();
    }

    public static class GateDecision {
        private final boolean allowed;
        private final String action;
        private final String reason;
        private final boolean approvalRequired;

        private GateDecision(boolean allowed, String action, String reason, boolean approvalRequired) {
            this.allowed = allowed;
            this.action = action;
            this.reason = reason;
            this.approvalRequired = approvalRequired;
        }

        public static GateDecision allowed() {
            return new GateDecision(true, "ALLOWED", null, false);
        }

        public static GateDecision blocked(String reason) {
            return new GateDecision(false, "BLOCKED", reason, false);
        }

        public static GateDecision review(boolean continueToLlm, String reason) {
            return new GateDecision(true, "REVIEW", reason, true);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getAction() {
            return action;
        }

        public String getReason() {
            return reason;
        }

        public boolean isApprovalRequired() {
            return approvalRequired;
        }
    }
}

