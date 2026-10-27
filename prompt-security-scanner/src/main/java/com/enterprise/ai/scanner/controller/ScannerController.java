package com.enterprise.ai.scanner.controller;

import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.ScanRequest;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.scanner.service.ScannerService;
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
@Tag(name = "Prompt Security Scanner", description = "Endpoints for threat detection, PII extraction, secret scanning, and redaction")
public class ScannerController {

    private final ScannerService scannerService;

    public ScannerController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/api/v1/scan")
    @Operation(summary = "Scan prompt for security violations", description = "Evaluates prompt text across PII, secrets, credentials, prompt injections, and confidential markings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scan evaluation completed",
                    content = @Content(schema = @Schema(implementation = ScanResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or constraint failure")
    })
    public ResponseEntity<ScanResult> scan(@Valid @RequestBody ScanRequest request) {
        ScanResult result = scannerService.scan(request.getRequestId(), request.getPrompt());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/v1/scan/redact")
    @Operation(summary = "Sanitize and redact sensitive data", description = "Redacts detected PII, API tokens, and confidential matches using token masks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prompt successfully sanitized",
                    content = @Content(schema = @Schema(implementation = RedactResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<RedactResult> redact(@Valid @RequestBody ScanRequest request) {
        RedactResult result = scannerService.redact(request.getRequestId(), request.getPrompt());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/scan/health")
    @Operation(summary = "Service health status", description = "Returns operational status and registered service name.")
    public ResponseEntity<Map<String, String>> serviceHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "prompt-security-scanner",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Root health status", description = "Standard root health check probe.")
    public ResponseEntity<Map<String, String>> health() {
        return serviceHealth();
    }
}
