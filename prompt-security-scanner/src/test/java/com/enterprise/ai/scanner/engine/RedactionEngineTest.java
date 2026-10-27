package com.enterprise.ai.scanner.engine;

import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.scanner.detector.DetectorMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redaction Engine Unit Tests")
class RedactionEngineTest {

    private RedactionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RedactionEngine();
    }

    @Test
    @DisplayName("Should cleanly redact matches without offset corruption")
    void testBoundaryAwareRedaction() {
        String prompt = "Client SSN 123-45-6789 and Card 4111 2222 3333 4444 submitted.";
        
        DetectorMatch ssnMatch = new DetectorMatch(
                Violation.builder().type("PII_SSN").severity(Severity.HIGH).build(),
                "123-45-6789", 11, 22
        );

        DetectorMatch cardMatch = new DetectorMatch(
                Violation.builder().type("PII_CREDIT_CARD").severity(Severity.CRITICAL).build(),
                "4111 2222 3333 4444", 32, 51
        );

        RedactResult result = engine.redact(prompt, List.of(ssnMatch, cardMatch));

        assertNotNull(result);
        assertTrue(result.getRedactedPrompt().contains("[REDACTED_SSN]"));
        assertTrue(result.getRedactedPrompt().contains("[REDACTED_CARD]"));
        assertFalse(result.getRedactedPrompt().contains("123-45-6789"));
        assertFalse(result.getRedactedPrompt().contains("4111 2222 3333 4444"));
    }

    @Test
    @DisplayName("Should return unchanged prompt when matches list is empty")
    void testEmptyMatches() {
        String prompt = "Clean business prompt.";
        RedactResult result = engine.redact(prompt, List.of());

        assertEquals(prompt, result.getRedactedPrompt());
    }
}
