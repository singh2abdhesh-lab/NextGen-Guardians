package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.DashboardOverview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing operational metrics, usage analytics, and cost figures
 * for the executive governance dashboard.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Executive Dashboard", description = "Endpoints for aggregated platform metrics, model spend, and usage trends")
public class DashboardController {

    private final AuditService auditService;

    public DashboardController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get dashboard overview metrics", description = "Returns total prompts, blocked count, pending approvals, total spend, and risk/action distributions.")
    @ApiResponse(responseCode = "200", description = "Overview metrics generated successfully",
            content = @Content(schema = @Schema(implementation = DashboardOverview.class)))
    public ResponseEntity<DashboardOverview> overview() {
        return ResponseEntity.ok(auditService.getOverview());
    }

    @GetMapping("/usage/models")
    @Operation(summary = "Get model usage and cost metrics", description = "Aggregates request volumes and dollar costs grouped by LLM model over given timeframe.")
    public ResponseEntity<List<Map<String, Object>>> usageByModel(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.usageByModel(days));
    }

    @GetMapping("/usage/users")
    @Operation(summary = "Get user usage volume", description = "Aggregates request count per user over designated days.")
    public ResponseEntity<List<Map<String, Object>>> usageByUser(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.usageByUser(days));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get prompt category distribution", description = "Returns distribution breakdown across prompt categories.")
    public ResponseEntity<Map<String, Long>> promptCategoryDistribution() {
        return ResponseEntity.ok(auditService.getPromptCategoryDistribution());
    }

    @GetMapping("/compliance")
    @Operation(summary = "Get dashboard compliance summary", description = "Returns high-level compliance rates and violations summary.")
    public ResponseEntity<Map<String, Object>> complianceSummary(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.getComplianceReport(days));
    }
}
