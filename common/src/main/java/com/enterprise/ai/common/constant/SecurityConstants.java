package com.enterprise.ai.common.constant;

import java.util.regex.Pattern;

/**
 * Enterprise AI Guardian - Core Security Thresholds, Weights, and Detection Patterns.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // --- Multi-Factor Risk Matrix Weights (Sum = 1.0) ---
    // Data sensitivity dominates so that a high-sensitivity request (>= 90) from a
    // low-clearance role reaches CRITICAL (>= 80) on its own; a weighted average with
    // data at 0.40 caps the composite at ~77 in that scenario, making CRITICAL unreachable.
    public static final double WEIGHT_DATA_SENSITIVITY = 0.50;
    public static final double WEIGHT_USER_ROLE = 0.25;
    public static final double WEIGHT_PROMPT_CATEGORY = 0.20;
    public static final double WEIGHT_TARGET_MODEL = 0.05;

    // --- Score Thresholds ---
    public static final int SCORE_MAX = 100;
    public static final int SCORE_MIN = 0;
    public static final int SENSITIVITY_BLOCK_THRESHOLD = 70;
    public static final int RISK_SCORE_CRITICAL_THRESHOLD = 80;
    public static final int RISK_SCORE_HIGH_THRESHOLD = 60;
    public static final int RISK_SCORE_MEDIUM_THRESHOLD = 30;

    // --- Quality Evaluation Thresholds ---
    public static final double MAX_TOLERABLE_HALLUCINATION_SCORE = 0.50;
    public static final double MAX_TOLERABLE_TOXICITY_SCORE = 0.35;

    // --- Redaction Constants ---
    public static final String REDACTION_MASK_DEFAULT = "[REDACTED]";
    public static final String REDACTION_MASK_SSN = "[REDACTED_SSN]";
    public static final String REDACTION_MASK_CARD = "[REDACTED_CARD]";
    public static final String REDACTION_MASK_EMAIL = "[REDACTED_EMAIL]";
    public static final String REDACTION_MASK_KEY = "[REDACTED_API_KEY]";

    // --- Compiled Regex Detection Patterns ---
    public static final Pattern REGEX_SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    public static final Pattern REGEX_CREDIT_CARD = Pattern.compile("\\b(?:\\d{4}[ -]?){3}\\d{4}\\b");
    public static final Pattern REGEX_EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    public static final Pattern REGEX_AWS_KEY = Pattern.compile("AKIA[0-9A-Z]{16}");
    public static final Pattern REGEX_OPENAI_KEY = Pattern.compile("sk-[a-zA-Z0-9_-]{20,}");
}
