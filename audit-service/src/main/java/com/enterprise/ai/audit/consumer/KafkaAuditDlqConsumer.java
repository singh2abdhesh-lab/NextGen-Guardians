package com.enterprise.ai.audit.consumer;

import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consumer listening to the Dead Letter Queue (DLQ / DLT) topic for audit events
 * that exhausted all retry attempts or failed serialization checks.
 */
@Component
public class KafkaAuditDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaAuditDlqConsumer.class);

    private final AuditService auditService;
    private final AtomicLong dlqMessagesReceived = new AtomicLong(0);
    private final AtomicReference<Instant> lastDlqMessageTime = new AtomicReference<>(Instant.MIN);

    public KafkaAuditDlqConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Ingests poison pill records from Dead Letter Topic and stores forensic problem records.
     */
    @KafkaListener(
            topics = "${app.kafka.audit-topic:ai-guardian-audit-events}.DLT",
            groupId = "${spring.kafka.consumer.group-id:audit-service-group}-dlq",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeadLetterRecord(
            @Payload String payload,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_OFFSET, required = false) Long originalOffset) {

        dlqMessagesReceived.incrementAndGet();
        lastDlqMessageTime.set(Instant.now());

        log.error("CRITICAL: Received poison-pill message on DLQ. OriginalTopic={} Partition={} Offset={} Error='{}'",
                originalTopic, originalPartition, originalOffset, exceptionMessage);

        try {
            // Attempt to parse partially valid AuditEvent
            AuditEvent event;
            try {
                event = JsonUtils.fromJson(payload, AuditEvent.class);
            } catch (Exception ex) {
                event = AuditEvent.builder()
                        .requestId("dlq-" + UUID.randomUUID())
                        .prompt(payload != null && payload.length() > 500 ? payload.substring(0, 500) : payload)
                        .build();
            }

            event.setAction("DEAD_LETTER");
            event.setBlockReason("DLQ Expiration: " + (exceptionMessage != null ? exceptionMessage : "Failed retry limits"));
            event.setTimestamp(LocalDateTime.now());
            event.setApprovalRequired(true);

            auditService.saveEvent(event);
            log.info("Persisted DLQ forensic record for requestId={}", event.getRequestId());
        } catch (Exception e) {
            log.error("Fatal: failed to persist DLQ audit record to database: {}", e.getMessage(), e);
        }
    }

    public long getDlqMessagesReceived() {
        return dlqMessagesReceived.get();
    }

    public Instant getLastDlqMessageTime() {
        return lastDlqMessageTime.get();
    }
}
