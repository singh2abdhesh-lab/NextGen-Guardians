package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.ApprovalRequest;
import com.enterprise.ai.common.dto.AuditEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing forensic audit event ingestion, querying, and approval workflows.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Audit Ingestion & Approvals", description = "REST APIs for ingesting audit events, querying historical logs, and human-in-the-loop review actions")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    // =========================================================================
    // Ingestion (REST Fallback Receiver)
    // =========================================================================

    @PostMapping("/api/v1/audit/events")
    @Operation(summary = "Ingest audit event (REST Fallback Receiver)", description = "Synchronously stores an audit event when Kafka broker is unreachable or bypassed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit event successfully ingested and stored",
                    content = @Content(schema = @Schema(implementation = AuditEvent.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<AuditEvent> createEvent(@Valid @RequestBody AuditEvent event) {
        var saved = auditService.saveEvent(event);
        return ResponseEntity.ok(auditService.toDto(saved));
    }

    @PostMapping("/api/v1/audit/events/batch")
    @Operation(summary = "Batch ingest audit events", description = "Synchronously persists multiple audit events in batch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch audit events ingested successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuditEvent.class))))
    })
    public ResponseEntity<List<AuditEvent>> createEventsBatch(@RequestBody List<AuditEvent> events) {
        return ResponseEntity.ok(auditService.saveEventsBatch(events));
    }

    // =========================================================================
    // Querying
    // =========================================================================

    @GetMapping("/api/v1/audit/events")
    @Operation(summary = "List recent audit events", description = "Returns most recent audit event records up to designated limit.")
    public ResponseEntity<List<AuditEvent>> listEvents(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "size", required = false) Integer size) {
        int effectiveLimit = limit != null ? limit : (size != null ? size : 100);
        return ResponseEntity.ok(auditService.listEvents(effectiveLimit));
    }

    @GetMapping("/api/v1/audit/events/{id}")
    @Operation(summary = "Get audit event by ID", description = "Retrieves an immutable audit event by its primary database key.")
    public ResponseEntity<AuditEvent> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getEventById(id));
    }

    @GetMapping("/api/v1/audit/events/request/{requestId}")
    @Operation(summary = "Get audit event by Request ID", description = "Retrieves an audit event by correlation request UUID.")
    public ResponseEntity<AuditEvent> getEventByRequestId(@PathVariable String requestId) {
        return ResponseEntity.ok(auditService.getEventByRequestId(requestId));
    }

    @GetMapping("/api/v1/audit/events/alerts")
    @Operation(summary = "List high and critical risk alerts", description = "Queries audit logs for events flagged as HIGH or CRITICAL risk.")
    public ResponseEntity<List<AuditEvent>> getAlerts() {
        return ResponseEntity.ok(auditService.getAlerts());
    }

    @GetMapping("/api/v1/audit/events/user/{userId}")
    @Operation(summary = "List events by user ID", description = "Filters historical audit logs for a specific user.")
    public ResponseEntity<List<AuditEvent>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.getByUser(userId));
    }

    @GetMapping("/api/v1/audit/events/pending")
    @Operation(summary = "List pending approvals", description = "Retrieves flagged audit items currently awaiting human-in-the-loop review.")
    public ResponseEntity<List<AuditEvent>> getPending() {
        return ResponseEntity.ok(auditService.getPendingApprovals());
    }

    // =========================================================================
    // Approval Workflow
    // =========================================================================

    @PostMapping("/api/v1/audit/events/{id}/approve")
    @Operation(summary = "Approve flagged event", description = "Authorizes a held prompt/completion event.")
    public ResponseEntity<AuditEvent> approve(@PathVariable Long id,
                                               @RequestParam(defaultValue = "admin") String approver,
                                               @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(auditService.approve(id, approver, reason));
    }

    @PostMapping("/api/v1/audit/events/{id}/reject")
    @Operation(summary = "Reject flagged event", description = "Terminates and blocks a held prompt/completion event.")
    public ResponseEntity<AuditEvent> reject(@PathVariable Long id,
                                              @RequestParam(defaultValue = "admin") String approver,
                                              @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(auditService.reject(id, approver, reason));
    }

    @PostMapping("/api/v1/audit/approvals")
    @Operation(summary = "Process approval decision payload", description = "Resolves an audit item using standard ApprovalRequest body.")
    public ResponseEntity<AuditEvent> processApproval(@Valid @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(auditService.processApproval(request));
    }

    // =========================================================================
    // Health Probes
    // =========================================================================

    @GetMapping("/api/v1/audit/health")
    @Operation(summary = "Service health probe", description = "Returns operational status and service identifier.")
    public ResponseEntity<Map<String, String>> serviceHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "audit-service",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Root health probe", description = "Standard root health check.")
    public ResponseEntity<Map<String, String>> health() {
        return serviceHealth();
    }
}
