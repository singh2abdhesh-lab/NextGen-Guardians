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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asynchronous Kafka consumer that subscribes to AI Guardian audit events
 * and persists them into the immutable audit database with retry and DLQ protection.
 */
@Component
public class KafkaAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaAuditConsumer.class);

    private final AuditService auditService;
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesProcessed = new AtomicLong(0);
    private final AtomicLong processingErrors = new AtomicLong(0);
    private final AtomicReference<Instant> lastProcessedTime = new AtomicReference<>(Instant.MIN);

    public KafkaAuditConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Listens to the audit topic and processes serialized AuditEvent payloads.
     *
     * @param message serialized AuditEvent JSON payload
     * @param partition source Kafka partition
     * @param offset source record offset
     */
    @KafkaListener(
            topics = "${app.kafka.audit-topic:ai-guardian-audit-events}",
            groupId = "${spring.kafka.consumer.group-id:audit-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload String message,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(name = KafkaHeaders.OFFSET, required = false) Long offset) {

        messagesReceived.incrementAndGet();

        if (message == null || message.isBlank()) {
            processingErrors.incrementAndGet();
            throw new IllegalArgumentException("Received empty or blank Kafka audit event payload");
        }

        try {
            log.info("Consuming audit event from partition={} offset={} (length={})",
                    partition, offset, message.length());

            AuditEvent event = JsonUtils.fromJson(message, AuditEvent.class);
            if (event.getRequestId() == null || event.getRequestId().isBlank()) {
                throw new IllegalArgumentException("AuditEvent missing required requestId");
            }

            auditService.saveEvent(event);
            messagesProcessed.incrementAndGet();
            lastProcessedTime.set(Instant.now());
            log.info("Successfully persisted Kafka audit event for requestId={}", event.getRequestId());
        } catch (Exception ex) {
            processingErrors.incrementAndGet();
            log.error("Failed to process audit event from Kafka partition={} offset={}: {}",
                    partition, offset, ex.getMessage());
            // Re-throw to trigger configured Retry and Dead Letter Queue (DLQ) recoverer
            throw new RuntimeException("Kafka audit processing failed: " + ex.getMessage(), ex);
        }
    }

    public long getMessagesReceived() {
        return messagesReceived.get();
    }

    public long getMessagesProcessed() {
        return messagesProcessed.get();
    }

    public long getProcessingErrors() {
        return processingErrors.get();
    }

    public Instant getLastProcessedTime() {
        return lastProcessedTime.get();
    }
}
