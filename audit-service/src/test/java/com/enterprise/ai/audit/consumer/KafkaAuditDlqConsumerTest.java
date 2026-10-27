package com.enterprise.ai.audit.consumer;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Audit DLQ Consumer Unit Tests")
class KafkaAuditDlqConsumerTest {

    @Mock
    private AuditService auditService;

    private KafkaAuditDlqConsumer dlqConsumer;

    @BeforeEach
    void setUp() {
        dlqConsumer = new KafkaAuditDlqConsumer(auditService);
    }

    @Test
    @DisplayName("Consuming poison pill stores forensic event with DEAD_LETTER action")
    void testConsumeDlqRecord() {
        String failedPayload = "{\"prompt\": \"corrupted stream payload\"}";

        when(auditService.saveEvent(any())).thenReturn(new AuditEventEntity());

        dlqConsumer.consumeDeadLetterRecord(
                failedPayload,
                "DeserializationException: invalid token",
                "ai-guardian-audit-events",
                1,
                555L
        );

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService, times(1)).saveEvent(captor.capture());

        AuditEvent saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("DEAD_LETTER", saved.getAction());
        assertTrue(saved.getBlockReason().contains("DeserializationException"));
        assertTrue(saved.isApprovalRequired());
        assertEquals(1, dlqConsumer.getDlqMessagesReceived());
    }
}
