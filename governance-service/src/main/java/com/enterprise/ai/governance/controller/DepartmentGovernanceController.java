package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.DepartmentGovernanceDto;
import com.enterprise.ai.governance.service.DepartmentGovernanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/departments")
@CrossOrigin(origins = "*")
@Tag(name = "Department Governance", description = "Endpoints for department token quotas, cost caps, model whitelists, and emergency overrides")
public class DepartmentGovernanceController {

    private final DepartmentGovernanceService departmentService;

    public DepartmentGovernanceController(DepartmentGovernanceService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "List all department governance policies and quota ceilings")
    public ResponseEntity<List<DepartmentGovernanceDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department governance rules by ID")
    public ResponseEntity<DepartmentGovernanceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Save or update department governance profile")
    public ResponseEntity<DepartmentGovernanceDto> saveDepartment(@Valid @RequestBody DepartmentGovernanceDto dto) {
        return ResponseEntity.ok(departmentService.saveDepartment(dto));
    }

    @PostMapping("/{id}/emergency-override")
    @Operation(summary = "Toggle Emergency Override to bypass quotas/hours temporarily")
    public ResponseEntity<DepartmentGovernanceDto> toggleEmergencyOverride(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestParam(required = false, defaultValue = "Operational incident mitigation") String reason,
            @RequestParam(required = false, defaultValue = "4") int durationHours) {
        return ResponseEntity.ok(departmentService.toggleEmergencyOverride(id, active, reason, durationHours));
    }

    @PostMapping("/{name}/spend")
    @Operation(summary = "Increment department accrued cost and check budget limits")
    public ResponseEntity<Map<String, String>> recordSpend(
            @PathVariable String name,
            @RequestParam double costUsd) {
        departmentService.recordSpend(name, costUsd);
        return ResponseEntity.ok(Map.of("message", "Spend recorded successfully", "department", name));
    }
}
