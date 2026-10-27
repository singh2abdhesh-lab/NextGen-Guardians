package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.PolicySimulationRequest;
import com.enterprise.ai.governance.dto.PolicySimulationResponse;
import com.enterprise.ai.governance.service.PolicySimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/governance/simulator")
@CrossOrigin(origins = "*")
@Tag(name = "Policy Simulator", description = "Endpoints for simulating AI governance evaluation across user, department, model, and prompt")
public class PolicySimulatorController {

    private final PolicySimulatorService simulatorService;

    public PolicySimulatorController(PolicySimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @PostMapping("/run")
    @Operation(summary = "Simulate AI Policy Evaluation", description = "Runs a dry-run prompt query through department constraints, model allowlists, and active AI policies to project the expected decision, risk score, approval requirements, and triggered rules.")
    public ResponseEntity<PolicySimulationResponse> runSimulation(@Valid @RequestBody PolicySimulationRequest request) {
        return ResponseEntity.ok(simulatorService.simulate(request));
    }
}
