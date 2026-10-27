package com.enterprise.ai.quality.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hallucination Detector Unit Tests")
class HallucinationDetectorTest {

    private HallucinationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new HallucinationDetector();
    }

    @Test
    @DisplayName("Clean factual response should yield 0.0 hallucination score")
    void testCleanResponse() {
        String cleanResponse = "Spring Boot 3.3 requires Java 17 and uses Jakarta EE 10 specifications.";
        double score = detector.calculateHallucinationScore(cleanResponse);
        List<String> flags = detector.detectHallucinationFlags(cleanResponse);

        assertEquals(0.0, score);
        assertTrue(flags.isEmpty());
    }

    @Test
    @DisplayName("Fabricated high-entropy URL should trigger hallucination signal")
    void testFabricatedUrl() {
        String responseWithFakeUrl = "You can view the full documentation at https://randomfake-ai-domain.org/docs/v1/generated_hallucinated_path_2910390192830129";
        double score = detector.calculateHallucinationScore(responseWithFakeUrl);
        List<String> flags = detector.detectHallucinationFlags(responseWithFakeUrl);

        assertTrue(score >= 0.40);
        assertFalse(flags.isEmpty());
        assertTrue(flags.get(0).contains("fabricated"));
    }

    @Test
    @DisplayName("Ungrounded statistical citation and overconfidence markers increase score")
    void testCitationAndOverconfidence() {
        String response = "According to a 2024 study, this model is guaranteed 100% accurate and indisputable truth.";
        double score = detector.calculateHallucinationScore(response);
        List<String> flags = detector.detectHallucinationFlags(response);

        assertTrue(score >= 0.50);
        assertEquals(2, flags.size());
    }

    @Test
    @DisplayName("Empty or null response returns zero score")
    void testNullOrBlankResponse() {
        assertEquals(0.0, detector.calculateHallucinationScore(null));
        assertEquals(0.0, detector.calculateHallucinationScore("   "));
        assertTrue(detector.detectHallucinationFlags(null).isEmpty());
    }
}
