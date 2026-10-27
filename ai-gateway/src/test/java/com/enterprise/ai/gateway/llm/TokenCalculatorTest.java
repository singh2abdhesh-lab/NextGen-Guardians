package com.enterprise.ai.gateway.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token Calculator Unit Tests")
class TokenCalculatorTest {

    private TokenCalculator tokenCalculator;

    @BeforeEach
    void setUp() {
        tokenCalculator = new TokenCalculator();
    }

    @Test
    @DisplayName("Empty or null text returns 0 tokens")
    void testEmptyText() {
        assertEquals(0, tokenCalculator.calculateTokens(null));
        assertEquals(0, tokenCalculator.calculateTokens(""));
    }

    @Test
    @DisplayName("Standard sentence calculates realistic token count")
    void testStandardSentence() {
        String prompt = "How do I configure Spring WebFlux security in Java 17?";
        int tokens = tokenCalculator.calculateTokens(prompt);
        assertTrue(tokens >= 8 && tokens <= 15, "Expected 8-15 tokens, got: " + tokens);
    }

    @Test
    @DisplayName("calculateMessageTokens includes OpenAI frame overhead")
    void testMessageOverhead() {
        String content = "Hello world";
        int contentTokens = tokenCalculator.calculateTokens(content);
        int totalTokens = tokenCalculator.calculateMessageTokens("user", content);

        assertTrue(totalTokens > contentTokens);
        assertEquals(contentTokens + tokenCalculator.calculateTokens("user") + 3, totalTokens);
    }
}
