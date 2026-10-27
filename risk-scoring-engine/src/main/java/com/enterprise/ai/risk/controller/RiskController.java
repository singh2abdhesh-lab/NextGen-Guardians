package com.enterprise.ai.risk.controller;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.risk.service.RiskScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Risk Scoring Engine", description = "Endpoints for multi-factor weighted risk assessment and policy action determination")
public class RiskController {

    private final RiskScoringService riskScoringService;

    public RiskController(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    @PostMapping({"/api/v1/risk/assess", "/api/v1/risk/score"})
    @Operation(summary = "Assess request risk", description = "Evaluates composite risk across data sensitivity (40%), user role (25%), prompt category (20%), and model (15%).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Risk assessment calculated successfully",
                    content = @Content(schema = @Schema(implementation = RiskScore.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or constraint failure")
    })
    public ResponseEntity<RiskScore> assess(@Valid @RequestBody RiskAssessRequest request) {
        RiskScore score = riskScoringService.assess(request);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/api/v1/risk/health")
    @Operation(summary = "Service health probe", description = "Returns operational status and registered service name.")
    public ResponseEntity<Map<String, String>> serviceHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "risk-scoring-engine",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Root health probe", description = "Standard root health check.")
    public ResponseEntity<Map<String, String>> health() {
        return serviceHealth();
    }
}
