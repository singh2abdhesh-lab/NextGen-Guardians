package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.PromptTemplateDto;
import com.enterprise.ai.governance.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/templates")
@CrossOrigin(origins = "*")
@Tag(name = "Prompt Template Management", description = "Endpoints for creating, managing, versioning, and sharing enterprise prompt templates")
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    public PromptTemplateController(PromptTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @Operation(summary = "List all prompt templates")
    public ResponseEntity<List<PromptTemplateDto>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/favorites")
    @Operation(summary = "List bookmarked/favorite prompt templates")
    public ResponseEntity<List<PromptTemplateDto>> getFavorites() {
        return ResponseEntity.ok(templateService.getFavorites());
    }

    @GetMapping("/search")
    @Operation(summary = "Search templates by title or description")
    public ResponseEntity<List<PromptTemplateDto>> searchTemplates(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(templateService.searchTemplates(q));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter templates by category")
    public ResponseEntity<List<PromptTemplateDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(templateService.getByCategory(category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prompt template by ID")
    public ResponseEntity<PromptTemplateDto> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PostMapping
    @Operation(summary = "Create new prompt template with variable extraction")
    public ResponseEntity<PromptTemplateDto> createTemplate(
            @Valid @RequestBody PromptTemplateDto dto,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "prompt-engineer") String author) {
        return ResponseEntity.ok(templateService.createTemplate(dto, author));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template and increment version")
    public ResponseEntity<PromptTemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody PromptTemplateDto dto) {
        return ResponseEntity.ok(templateService.updateTemplate(id, dto));
    }

    @PostMapping("/{id}/favorite")
    @Operation(summary = "Toggle favorite/starred status")
    public ResponseEntity<PromptTemplateDto> toggleFavorite(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.toggleFavorite(id));
    }

    @PostMapping("/{id}/use")
    @Operation(summary = "Increment template usage statistics")
    public ResponseEntity<PromptTemplateDto> recordUsage(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.incrementUsage(id));
    }

    @PostMapping("/import")
    @Operation(summary = "Batch import prompt templates from JSON")
    public ResponseEntity<List<PromptTemplateDto>> importTemplates(
            @RequestBody List<PromptTemplateDto> dtos,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "importer") String author) {
        return ResponseEntity.ok(templateService.importTemplates(dtos, author));
    }

    @GetMapping("/export")
    @Operation(summary = "Export all approved prompt templates to JSON")
    public ResponseEntity<List<PromptTemplateDto>> exportTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template")
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(Map.of("message", "Template deleted", "id", id.toString()));
    }
}
