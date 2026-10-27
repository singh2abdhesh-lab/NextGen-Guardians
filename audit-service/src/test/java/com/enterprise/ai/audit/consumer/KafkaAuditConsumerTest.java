package com.enterprise.ai.audit.consumer;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Audit Consumer Unit Tests")
class KafkaAuditConsumerTest {

    @Mock
    private AuditService auditService;

    private KafkaAuditConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaAuditConsumer(auditService);
    }

    @Test
    @DisplayName("Valid AuditEvent JSON message should be parsed and saved")
    void testConsumeValidMessage() {
        AuditEvent event = AuditEvent.builder()
                .requestId("req-kafka-1")
                .userId("user-kafka")
                .action("ALLOW")
                .riskScore(20)
                .riskLevel("LOW")
                .build();

        String jsonPayload = JsonUtils.toJson(event);

        when(auditService.saveEvent(any(AuditEvent.class))).thenReturn(new AuditEventEntity());

        consumer.consumeAuditEvent(jsonPayload, 0, 100L);

        verify(auditService, times(1)).saveEvent(any(AuditEvent.class));
        assertEquals(1, consumer.getMessagesProcessed());
        assertEquals(0, consumer.getProcessingErrors());
    }

    @Test
    @DisplayName("Empty or null message throws exception to activate DLQ")
    void testConsumeEmptyMessageThrows() {
        assertThrows(IllegalArgumentException.class, () -> consumer.consumeAuditEvent("", 0, 101L));
        assertThrows(IllegalArgumentException.class, () -> consumer.consumeAuditEvent(null, 0, 102L));

        verify(auditService, never()).saveEvent(any());
        assertEquals(2, consumer.getProcessingErrors());
    }

    @Test
    @DisplayName("Malformed JSON message throws RuntimeException to activate retry & DLQ")
    void testConsumeMalformedJsonThrows() {
        assertThrows(RuntimeException.class, () -> consumer.consumeAuditEvent("{malformed json", 0, 103L));

        verify(auditService, never()).saveEvent(any());
        assertEquals(1, consumer.getProcessingErrors());
    }
}
