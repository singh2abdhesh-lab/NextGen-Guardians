package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.AiPolicyDto;
import com.enterprise.ai.governance.dto.PolicyAuditHistoryDto;
import com.enterprise.ai.governance.service.PolicyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/policies")
@CrossOrigin(origins = "*")
@Tag(name = "Policy Management", description = "CRUD APIs for AI Policies, versioning, rollback, and audit history")
public class PolicyController {

    private final PolicyManagementService policyService;

    public PolicyController(PolicyManagementService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    @Operation(summary = "List all AI policies")
    public ResponseEntity<List<AiPolicyDto>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/active")
    @Operation(summary = "List all active, enabled AI policies ordered by priority")
    public ResponseEntity<List<AiPolicyDto>> getActivePolicies() {
        return ResponseEntity.ok(policyService.getActivePolicies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<AiPolicyDto> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @PostMapping
    @Operation(summary = "Create new AI Policy with initial version (v1)")
    public ResponseEntity<AiPolicyDto> createPolicy(
            @Valid @RequestBody AiPolicyDto dto,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "secops-admin") String operator) {
        return ResponseEntity.ok(policyService.createPolicy(dto, operator));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update policy definition and increment version")
    public ResponseEntity<AiPolicyDto> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody AiPolicyDto dto,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "secops-admin") String operator) {
        return ResponseEntity.ok(policyService.updatePolicy(id, dto, operator));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable policy")
    public ResponseEntity<AiPolicyDto> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "secops-admin") String operator) {
        return ResponseEntity.ok(policyService.toggleEnabled(id, enabled, operator));
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "Rollback policy to a previous version snapshot")
    public ResponseEntity<AiPolicyDto> rollbackPolicy(
            @PathVariable Long id,
            @RequestParam int targetVersion,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "secops-admin") String operator) {
        return ResponseEntity.ok(policyService.rollbackPolicy(id, targetVersion, operator));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete policy and record audit entry")
    public ResponseEntity<Map<String, String>> deletePolicy(
            @PathVariable Long id,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "secops-admin") String operator) {
        policyService.deletePolicy(id, operator);
        return ResponseEntity.ok(Map.of("message", "Policy deleted successfully", "id", id.toString()));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get audit history for specific policy")
    public ResponseEntity<List<PolicyAuditHistoryDto>> getPolicyHistory(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getAuditHistory(id));
    }

    @GetMapping("/history")
    @Operation(summary = "Get all recent policy change audit records")
    public ResponseEntity<List<PolicyAuditHistoryDto>> getAllHistory() {
        return ResponseEntity.ok(policyService.getRecentAuditHistory());
    }
}
