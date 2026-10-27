package com.enterprise.ai.quality.controller;

import com.enterprise.ai.common.dto.QualityCheckRequest;
import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.quality.service.QualityService;
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

/**
 * REST controller exposing endpoints for LLM response quality verification,
 * hallucination scoring, secret echo detection, and policy compliance.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Response Quality Checker", description = "Endpoints for hallucination detection, secret echo verification, PII leak audits, and policy enforcement")
public class QualityController {

    private final QualityService qualityService;

    public QualityController(QualityService qualityService) {
        this.qualityService = qualityService;
    }

    @PostMapping("/api/v1/quality/check")
    @Operation(
            summary = "Perform response quality and safety check",
            description = "Analyzes model output against input prompt to detect hallucinations, echoed credentials, PII leakage, and enterprise policy breaches."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quality check executed successfully",
                    content = @Content(schema = @Schema(implementation = QualityResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or constraint failure"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal error during quality evaluation"
            )
    })
    public ResponseEntity<QualityResult> check(@Valid @RequestBody QualityCheckRequest request) {
        QualityResult result = qualityService.check(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/quality/health")
    @Operation(summary = "Service health probe", description = "Returns operational health status and metadata for response quality checker.")
    public ResponseEntity<Map<String, String>> serviceHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "response-quality-checker",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Root health probe", description = "Standard root health check endpoint.")
    public ResponseEntity<Map<String, String>> health() {
        return serviceHealth();
    }
}
