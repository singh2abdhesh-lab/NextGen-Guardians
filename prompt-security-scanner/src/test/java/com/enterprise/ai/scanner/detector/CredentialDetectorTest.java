package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Credential Detector Unit Tests")
class CredentialDetectorTest {

    private CredentialDetector detector;

    @BeforeEach
    void setUp() {
        detector = new CredentialDetector();
    }

    @Test
    @DisplayName("Should detect database connection string with embedded password")
    void testDetectDbConnectionString() {
        String prompt = "Connect with jdbc:postgresql://admin:superSecret123@prod-db.corp:5432/finance_db";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CREDENTIAL_DB_CONNECTION".equals(v.getType())));
        assertTrue(violations.stream().anyMatch(v -> v.getSeverity() == Severity.CRITICAL));
    }

    @Test
    @DisplayName("Should detect JWT Bearer tokens")
    void testDetectJwtToken() {
        String prompt = "Header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CREDENTIAL_JWT_TOKEN".equals(v.getType())));
    }

    @Test
    @DisplayName("Should detect plaintext password assignment")
    void testDetectPlaintextPassword() {
        String prompt = "Configuration: db_password = 'ProductionMasterSecret2024!'";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CREDENTIAL_PASSWORD_PLAINTEXT".equals(v.getType())));
    }
}
