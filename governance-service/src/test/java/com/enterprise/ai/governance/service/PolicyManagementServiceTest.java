package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.AiPolicyDto;
import com.enterprise.ai.governance.entity.AiPolicyEntity;
import com.enterprise.ai.governance.entity.PolicyAuditHistoryEntity;
import com.enterprise.ai.governance.repository.AiPolicyRepository;
import com.enterprise.ai.governance.repository.PolicyAuditHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Policy Management Service Unit Tests")
class PolicyManagementServiceTest {

    @Mock
    private AiPolicyRepository policyRepository;

    @Mock
    private PolicyAuditHistoryRepository auditHistoryRepository;

    @Mock
    private NotificationService notificationService;

    private PolicyManagementService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyManagementService(policyRepository, auditHistoryRepository, notificationService);
    }

    @Test
    @DisplayName("Create Policy should save entity, assign version 1, and write audit history")
    void testCreatePolicy() {
        AiPolicyDto input = AiPolicyDto.builder()
                .policyKey("POL-TEST-001")
                .name("Test Policy")
                .category("SECURITY")
                .priority(10)
                .action("BLOCK")
                .build();

        when(policyRepository.existsByPolicyKey("POL-TEST-001")).thenReturn(false);
        when(policyRepository.save(any(AiPolicyEntity.class))).thenAnswer(invocation -> {
            AiPolicyEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return entity;
        });

        AiPolicyDto created = policyService.createPolicy(input, "admin-alex");

        assertNotNull(created);
        assertEquals(101L, created.getId());
        assertEquals(1, created.getVersion());
        verify(auditHistoryRepository, times(1)).save(any(PolicyAuditHistoryEntity.class));
        verify(notificationService, times(1)).sendAlert(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Update Policy should increment version and record audit diff")
    void testUpdatePolicy() {
        AiPolicyEntity existing = AiPolicyEntity.builder()
                .id(101L)
                .policyKey("POL-TEST-001")
                .name("Old Policy Name")
                .category("SECURITY")
                .priority(10)
                .version(1)
                .action("REVIEW")
                .enabled(true)
                .build();

        when(policyRepository.findById(101L)).thenReturn(Optional.of(existing));
        when(policyRepository.save(any(AiPolicyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiPolicyDto updateDto = AiPolicyDto.builder()
                .name("New Policy Name")
                .category("SECURITY")
                .priority(5)
                .action("BLOCK")
                .build();

        AiPolicyDto updated = policyService.updatePolicy(101L, updateDto, "admin-mark");

        assertNotNull(updated);
        assertEquals(2, updated.getVersion());
        assertEquals("New Policy Name", updated.getName());
        assertEquals("BLOCK", updated.getAction());
        verify(auditHistoryRepository, times(1)).save(any(PolicyAuditHistoryEntity.class));
    }

    @Test
    @DisplayName("Toggle Enabled should alter enabled status and audit action")
    void testToggleEnabled() {
        AiPolicyEntity existing = AiPolicyEntity.builder()
                .id(101L)
                .policyKey("POL-TEST-001")
                .name("Old Policy Name")
                .version(2)
                .enabled(true)
                .status("ACTIVE")
                .build();

        when(policyRepository.findById(101L)).thenReturn(Optional.of(existing));
        when(policyRepository.save(any(AiPolicyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiPolicyDto result = policyService.toggleEnabled(101L, false, "secops");

        assertFalse(result.getEnabled());
        assertEquals("SUSPENDED", result.getStatus());
        verify(auditHistoryRepository, times(1)).save(argThat(a -> "DISABLE".equals(a.getActionType())));
    }
}
