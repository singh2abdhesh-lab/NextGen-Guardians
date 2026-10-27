package com.enterprise.ai.common.util;

import com.enterprise.ai.common.constant.SecurityConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread-safe utility methods for string sanitization, token masking, and pattern anonymization.
 * Implements OWASP guidelines for PII masking and prompt injection defense.
 */
public final class SanitizationUtils {

    // Regex matching non-printable zero-width unicode characters used in prompt injection bypass attacks
    private static final Pattern ZERO_WIDTH_CHARS = Pattern.compile("[\\u200B-\\u200D\\uFEFF\\u0000-\\u001F\\u007F-\\u009F]");

    private SanitizationUtils() {
        // Prevent instantiation
    }

    /**
     * Strips non-printable and zero-width unicode characters to defend against prompt injection obfuscation.
     */
    public static String stripInvisibleChars(String input) {
        if (input == null || input.isEmpty()) return input;
        return ZERO_WIDTH_CHARS.matcher(input).replaceAll("");
    }

    /**
     * Partially masks sensitive text: retains the first 2 and last 2 characters, replacing the middle with ****.
     */
    public static String maskMiddle(String raw) {
        if (raw == null || raw.length() <= 4) {
            return "****";
        }
        return raw.substring(0, 2) + "****" + raw.substring(raw.length() - 2);
    }

    /**
     * Partially masks a Social Security Number, preserving only the last 4 digits (e.g., ***-**-1234).
     */
    public static String maskSsnPartial(String ssn) {
        if (ssn == null) return null;
        Matcher matcher = SecurityConstants.REGEX_SSN.matcher(ssn);
        return matcher.replaceAll(matchResult -> {
            String full = matchResult.group();
            String lastFour = full.substring(Math.max(0, full.length() - 4));
            return "***-**-" + lastFour;
        });
    }

    /**
     * Partially masks a Credit Card Number, retaining the first 4 and last 4 digits (e.g., 4111-****-****-4444).
     */
    public static String maskCreditCardPartial(String card) {
        if (card == null) return null;
        Matcher matcher = SecurityConstants.REGEX_CREDIT_CARD.matcher(card);
        return matcher.replaceAll(matchResult -> {
            String clean = matchResult.group().replaceAll("[ -]", "");
            if (clean.length() >= 8) {
                String firstFour = clean.substring(0, 4);
                String lastFour = clean.substring(clean.length() - 4);
                return firstFour + "-****-****-" + lastFour;
            }
            return SecurityConstants.REDACTION_MASK_CARD;
        });
    }

    /**
     * Partially masks email addresses (e.g., john.doe@enterprise.com -> j***e@enterprise.com).
     */
    public static String maskEmailPartial(String email) {
        if (email == null) return null;
        Matcher matcher = SecurityConstants.REGEX_EMAIL.matcher(email);
        return matcher.replaceAll(matchResult -> {
            String full = matchResult.group();
            int atIndex = full.indexOf('@');
            if (atIndex > 2) {
                String user = full.substring(0, atIndex);
                String domain = full.substring(atIndex);
                return user.charAt(0) + "***" + user.charAt(user.length() - 1) + domain;
            }
            return SecurityConstants.REDACTION_MASK_EMAIL;
        });
    }

    /**
     * Replaces all Social Security Numbers with the masked token.
     */
    public static String redactSsn(String input) {
        if (input == null || input.isEmpty()) return input;
        return SecurityConstants.REGEX_SSN.matcher(input).replaceAll(SecurityConstants.REDACTION_MASK_SSN);
    }

    /**
     * Replaces all Credit Card Numbers with the masked token.
     */
    public static String redactCreditCard(String input) {
        if (input == null || input.isEmpty()) return input;
        return SecurityConstants.REGEX_CREDIT_CARD.matcher(input).replaceAll(SecurityConstants.REDACTION_MASK_CARD);
    }

    /**
     * Replaces email addresses with the masked token.
     */
    public static String redactEmail(String input) {
        if (input == null || input.isEmpty()) return input;
        return SecurityConstants.REGEX_EMAIL.matcher(input).replaceAll(SecurityConstants.REDACTION_MASK_EMAIL);
    }

    /**
     * Replaces AWS access keys and OpenAI secret tokens with the masked token.
     */
    public static String redactSecrets(String input) {
        if (input == null || input.isEmpty()) return input;
        String sanitized = SecurityConstants.REGEX_AWS_KEY.matcher(input).replaceAll(SecurityConstants.REDACTION_MASK_KEY);
        return SecurityConstants.REGEX_OPENAI_KEY.matcher(sanitized).replaceAll(SecurityConstants.REDACTION_MASK_KEY);
    }

    /**
     * Applies full sanitization pipeline to an arbitrary string.
     */
    public static String sanitizeFull(String input) {
        if (input == null || input.isEmpty()) return input;
        String s = stripInvisibleChars(input);
        s = redactSsn(s);
        s = redactCreditCard(s);
        s = redactEmail(s);
        return redactSecrets(s);
    }
}
