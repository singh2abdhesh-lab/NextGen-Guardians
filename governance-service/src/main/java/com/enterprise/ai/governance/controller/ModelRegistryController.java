package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.ModelRegistrationDto;
import com.enterprise.ai.governance.service.ModelRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/models")
@CrossOrigin(origins = "*")
@Tag(name = "Model Registry", description = "Endpoints for registering, tracking, pricing, and health-checking enterprise LLMs")
public class ModelRegistryController {

    private final ModelRegistryService modelService;

    public ModelRegistryController(ModelRegistryService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    @Operation(summary = "List all registered models")
    public ResponseEntity<List<ModelRegistrationDto>> getAllModels() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @GetMapping("/active")
    @Operation(summary = "List approved active models")
    public ResponseEntity<List<ModelRegistrationDto>> getActiveModels() {
        return ResponseEntity.ok(modelService.getActiveModels());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get model by ID")
    public ResponseEntity<ModelRegistrationDto> getModelById(@PathVariable Long id) {
        return ResponseEntity.ok(modelService.getModelById(id));
    }

    @PostMapping
    @Operation(summary = "Register new LLM in enterprise catalog")
    public ResponseEntity<ModelRegistrationDto> registerModel(@Valid @RequestBody ModelRegistrationDto dto) {
        return ResponseEntity.ok(modelService.registerModel(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update registered model specifications or pricing")
    public ResponseEntity<ModelRegistrationDto> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelRegistrationDto dto) {
        return ResponseEntity.ok(modelService.updateModel(id, dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle model enabled/disabled status")
    public ResponseEntity<ModelRegistrationDto> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(modelService.toggleModelStatus(id, enabled));
    }

    @PostMapping("/{id}/health-check")
    @Operation(summary = "Execute model endpoint health check probe")
    public ResponseEntity<ModelRegistrationDto> pingHealthCheck(@PathVariable Long id) {
        return ResponseEntity.ok(modelService.pingHealthCheck(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete model from registry")
    public ResponseEntity<Map<String, String>> deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
        return ResponseEntity.ok(Map.of("message", "Model removed from registry", "id", id.toString()));
    }
}
