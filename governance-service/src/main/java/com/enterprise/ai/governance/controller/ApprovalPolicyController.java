package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.ApprovalPolicyDto;
import com.enterprise.ai.governance.service.ApprovalPolicyEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/approvals")
@CrossOrigin(origins = "*")
@Tag(name = "Approval Policy Engine", description = "Endpoints for configuring single, multi-level, parallel, and escalation approval workflows")
public class ApprovalPolicyController {

    private final ApprovalPolicyEngineService approvalPolicyService;

    public ApprovalPolicyController(ApprovalPolicyEngineService approvalPolicyService) {
        this.approvalPolicyService = approvalPolicyService;
    }

    @GetMapping
    @Operation(summary = "List all configured approval workflow policies")
    public ResponseEntity<List<ApprovalPolicyDto>> getAllApprovalPolicies() {
        return ResponseEntity.ok(approvalPolicyService.getAllApprovalPolicies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get approval policy by ID")
    public ResponseEntity<ApprovalPolicyDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalPolicyService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Save or update approval workflow policy")
    public ResponseEntity<ApprovalPolicyDto> savePolicy(@Valid @RequestBody ApprovalPolicyDto dto) {
        return ResponseEntity.ok(approvalPolicyService.saveApprovalPolicy(dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable approval workflow")
    public ResponseEntity<ApprovalPolicyDto> togglePolicy(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(approvalPolicyService.togglePolicy(id, enabled));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete approval workflow policy")
    public ResponseEntity<Map<String, String>> deletePolicy(@PathVariable Long id) {
        approvalPolicyService.deletePolicy(id);
        return ResponseEntity.ok(Map.of("message", "Approval policy removed", "id", id.toString()));
    }
}
