package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.AuditEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing compliance reports, forensic audit export,
 * and regulatory framework compliance assessments.
 */
@RestController
@RequestMapping("/api/v1/compliance")
@CrossOrigin(origins = "*")
@Tag(name = "Compliance & Reports", description = "Endpoints for enterprise compliance auditing, regulatory assessments, and forensic CSV export")
public class ComplianceController {

    private final AuditService auditService;

    public ComplianceController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/report")
    @Operation(summary = "Generate compliance report", description = "Generates comprehensive compliance audit assessment across NIST AI RMF, EU AI Act, HIPAA, and PCI-DSS.")
    public ResponseEntity<Map<String, Object>> getReport(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.getComplianceReport(days));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    @Operation(summary = "Export forensic audit trail as CSV", description = "Generates and streams an RFC 4180 compliant CSV export of audit records.")
    @ApiResponse(responseCode = "200", description = "CSV file stream generated")
    public ResponseEntity<String> exportCsv(@RequestParam(defaultValue = "30") int days) {
        String csv = auditService.generateCsvExport(days);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ai-guardian-audit-trail.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/frameworks")
    @Operation(summary = "List supported compliance frameworks", description = "Returns active regulatory frameworks mapped within Enterprise AI Guardian.")
    public ResponseEntity<List<Map<String, Object>>> getFrameworks() {
        List<Map<String, Object>> frameworks = List.of(
                Map.of(
                        "framework", "NIST AI RMF 1.0",
                        "scope", "Govern, Map, Measure, Manage",
                        "status", "ACTIVE",
                        "coverage", "Continuous prompt scanning, risk factor scoring, and human verification"
                ),
                Map.of(
                        "framework", "EU AI Act (2024)",
                        "scope", "High-Risk AI Systems, Transparency & Oversight",
                        "status", "ACTIVE",
                        "coverage", "Audit trail retention, model identification, and human oversight workflow"
                ),
                Map.of(
                        "framework", "HIPAA Security Rule",
                        "scope", "ePHI Safeguards",
                        "status", "ACTIVE",
                        "coverage", "Automated PII/PHI extraction and zero-trust redaction"
                ),
                Map.of(
                        "framework", "PCI-DSS v4.0",
                        "scope", "Primary Account Number (PAN) Protection",
                        "status", "ACTIVE",
                        "coverage", "Payment card token detection and redaction engine"
                ),
                Map.of(
                        "framework", "SOC 2 Type II",
                        "scope", "Security, Availability, Confidentiality",
                        "status", "ACTIVE",
                        "coverage", "Immutable forensic event logging and tamper-evident storage"
                )
        );
        return ResponseEntity.ok(frameworks);
    }

    @GetMapping("/audit-trail/{requestId}")
    @Operation(summary = "Get forensic audit record by Request ID", description = "Returns complete forensic event record for compliance investigations.")
    public ResponseEntity<AuditEvent> getAuditTrail(@PathVariable String requestId) {
        return ResponseEntity.ok(auditService.getEventByRequestId(requestId));
    }
}
