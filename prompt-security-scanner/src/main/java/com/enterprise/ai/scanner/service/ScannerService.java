package com.enterprise.ai.scanner.service;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.common.util.SanitizationUtils;
import com.enterprise.ai.scanner.detector.Detector;
import com.enterprise.ai.scanner.detector.DetectorMatch;
import com.enterprise.ai.scanner.engine.RedactionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core security scanning and redaction orchestrator.
 */
@Service
public class ScannerService {

    private static final Logger log = LoggerFactory.getLogger(ScannerService.class);

    private final List<Detector> detectors;
    private final RedactionEngine redactionEngine;

    public ScannerService(List<Detector> detectors, RedactionEngine redactionEngine) {
        this.detectors = detectors != null ? detectors : Collections.emptyList();
        this.redactionEngine = redactionEngine != null ? redactionEngine : new RedactionEngine();
        log.info("Initialized ScannerService with {} registered detectors", this.detectors.size());
    }

    /**
     * Inspects a prompt across all registered detectors, computing violation scores and blocking determination.
     */
    public ScanResult scan(String requestId, String prompt) {
        long startTime = System.currentTimeMillis();
        List<Violation> violations = new ArrayList<>();
        Set<String> detectedCategories = new LinkedHashSet<>();

        if (prompt != null && !prompt.isEmpty()) {
            for (Detector detector : detectors) {
                try {
                    List<Violation> detected = detector.detect(prompt);
                    if (detected != null && !detected.isEmpty()) {
                        violations.addAll(detected);
                        detectedCategories.add(detector.getCategory());
                    }
                } catch (Exception e) {
                    log.error("Detector '{}' failed evaluating request {}: {}", detector.getClass().getSimpleName(), requestId, e.getMessage(), e);
                }
            }
        }

        // Sum severity weights capped at SCORE_MAX (100)
        int sensitivityScore = Math.min(SecurityConstants.SCORE_MAX,
                violations.stream().mapToInt(v -> v.getSeverity().getWeight()).sum());

        // Block if any CRITICAL violation is detected or sensitivity reaches threshold (70)
        boolean blocked = violations.stream().anyMatch(v -> v.getSeverity() == Severity.CRITICAL)
                || sensitivityScore >= SecurityConstants.SENSITIVITY_BLOCK_THRESHOLD;

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Scan completed for request '{}' in {}ms. Blocked: {}, Score: {}, Violations: {}",
                requestId, elapsed, blocked, sensitivityScore, violations.size());

        return ScanResult.builder()
                .requestId(requestId)
                .blocked(blocked)
                .sensitivityScore(sensitivityScore)
                .detectedCategories(new ArrayList<>(detectedCategories))
                .violations(violations)
                .build();
    }

    /**
     * Scans and masks sensitive matches using the RedactionEngine.
     */
    public RedactResult redact(String requestId, String prompt) {
        List<DetectorMatch> allMatches = new ArrayList<>();

        if (prompt != null && !prompt.isEmpty()) {
            for (Detector detector : detectors) {
                try {
                    List<DetectorMatch> matches = detector.detectWithMatches(prompt);
                    if (matches != null && !matches.isEmpty()) {
                        allMatches.addAll(matches);
                    }
                } catch (Exception e) {
                    log.error("Detector '{}' failed generating matches for {}: {}", detector.getClass().getSimpleName(), requestId, e.getMessage(), e);
                }
            }
        }

        return redactionEngine.redact(prompt, allMatches);
    }

    /**
     * Backward-compatible static helper to mask a raw matched snippet.
     */
    public static String maskPattern(String raw) {
        return SanitizationUtils.maskMiddle(raw);
    }
}
