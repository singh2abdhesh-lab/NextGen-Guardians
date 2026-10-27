package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.UserGovernanceDto;
import com.enterprise.ai.governance.service.UserGovernanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/governance/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Governance", description = "Endpoints for user-specific AI token limits, role assignments, and restriction enforcement")
public class UserGovernanceController {

    private final UserGovernanceService userService;

    public UserGovernanceController(UserGovernanceService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all governed user profiles")
    public ResponseEntity<List<UserGovernanceDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user governance record by ID")
    public ResponseEntity<UserGovernanceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/lookup/{userId}")
    @Operation(summary = "Lookup governance record by User ID or email")
    public ResponseEntity<UserGovernanceDto> getByUserId(@PathVariable String userId) {
        UserGovernanceDto user = userService.getByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Save or update user governance profile")
    public ResponseEntity<UserGovernanceDto> saveUser(@Valid @RequestBody UserGovernanceDto dto) {
        return ResponseEntity.ok(userService.saveUser(dto));
    }

    @PostMapping("/{id}/restrict")
    @Operation(summary = "Restrict or reinstate user AI privileges")
    public ResponseEntity<UserGovernanceDto> toggleRestriction(
            @PathVariable Long id,
            @RequestParam boolean restricted,
            @RequestParam(required = false, defaultValue = "Policy violation or audit lock") String reason) {
        return ResponseEntity.ok(userService.toggleRestriction(id, restricted, reason));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user governance profile")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User governance profile removed", "id", id.toString()));
    }
}
