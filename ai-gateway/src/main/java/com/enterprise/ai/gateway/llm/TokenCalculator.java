package com.enterprise.ai.gateway.llm;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculates and estimates token counts for LLM prompts, chat completions,
 * and conversational message envelopes using enterprise heuristics.
 */
@Component
public class TokenCalculator {

    // Regex matching word tokens, punctuation clusters, numbers, and whitespace boundaries
    private static final Pattern TOKEN_REGEX = Pattern.compile("\\w+|[^\\w\\s]+|\\s+");

    /**
     * Estimates prompt token count.
     *
     * @param text prompt or text content
     * @return estimated token count (minimum 0)
     */
    public int calculateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Fast path for short texts
        int length = text.length();
        if (length < 8) {
            return Math.max(1, (int) Math.ceil(length / 3.0));
        }

        int count = 0;
        Matcher matcher = TOKEN_REGEX.matcher(text);
        while (matcher.find()) {
            String match = matcher.group();
            if (match.isBlank()) {
                // Whitespace generally folds into adjacent tokens unless large indentation
                if (match.length() > 4) {
                    count += match.length() / 4;
                }
            } else if (match.length() > 6) {
                // Longer compound words break into multiple sub-word tokens
                count += (int) Math.ceil(match.length() / 4.0);
            } else {
                count++;
            }
        }

        return Math.max(1, count);
    }

    /**
     * Calculates total tokens for an OpenAI Chat Completion message envelope.
     * Each message incurs approximately 3 overhead tokens (role, name, content delimiter).
     *
     * @param role    message role (e.g. "system", "user", "assistant")
     * @param content message text
     * @return total token count including envelope overhead
     */
    public int calculateMessageTokens(String role, String content) {
        int contentTokens = calculateTokens(content);
        int roleTokens = calculateTokens(role);
        // OpenAI message framing overhead: 3 tokens per message (<|im_start|>{role}\n{content}<|im_end|>)
        return contentTokens + roleTokens + 3;
    }
}

