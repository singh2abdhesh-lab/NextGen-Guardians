package com.enterprise.ai.gateway.publisher;

import com.enterprise.ai.common.dto.AuditEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Audit Producer Unit Tests")
class KafkaAuditProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaAuditProducer producer;

    @BeforeEach
    void setUp() {
        producer = new KafkaAuditProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "kafkaEnabled", true);
        ReflectionTestUtils.setField(producer, "auditTopic", "ai-guardian-audit-events");
    }

    @Test
    @DisplayName("Sending audit event delegates to KafkaTemplate and records success metric")
    void testSendAuditEventSuccess() throws Exception {
        AuditEvent event = AuditEvent.builder()
                .requestId("req-prod-1")
                .userId("tester")
                .action("ALLOW")
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("ai-guardian-audit-events", 0),
                0L, 0, 0L, 0, 0
        );
        SendResult<String, String> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq("ai-guardian-audit-events"), eq("req-prod-1"), any())).thenReturn(future);

        CompletableFuture<SendResult<String, String>> resultFuture = producer.sendAuditEvent(event);
        assertNotNull(resultFuture);
        SendResult<String, String> result = resultFuture.get();

        assertNotNull(result);
        assertEquals("ai-guardian-audit-events", result.getRecordMetadata().topic());
        assertEquals(1, producer.getSuccessfulDispatches());
        assertEquals(0, producer.getFailedDispatches());
    }

    @Test
    @DisplayName("Disabled producer returns exceptionally completed future")
    void testDisabledProducer() {
        ReflectionTestUtils.setField(producer, "kafkaEnabled", false);

        AuditEvent event = AuditEvent.builder().requestId("req-disabled").build();
        CompletableFuture<SendResult<String, String>> future = producer.sendAuditEvent(event);

        assertTrue(future.isCompletedExceptionally());
    }
}
