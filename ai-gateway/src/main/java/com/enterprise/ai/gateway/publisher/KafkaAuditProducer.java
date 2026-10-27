package com.enterprise.ai.gateway.publisher;

import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * High-throughput asynchronous Kafka producer dispatching immutable forensic audit events
 * with acknowledgement callbacks, correlation tracking, and delivery metrics.
 */
@Component
public class KafkaAuditProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaAuditProducer.class);

    private final Optional<KafkaTemplate<String, String>> kafkaTemplate;

    @Value("${ai.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${ai.kafka.topic:ai-guardian-audit-events}")
    private String auditTopic;

    private final AtomicLong successfulDispatches = new AtomicLong(0);
    private final AtomicLong failedDispatches = new AtomicLong(0);
    private final AtomicReference<Instant> lastDispatchTime = new AtomicReference<>(Instant.MIN);

    public KafkaAuditProducer(@Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = Optional.ofNullable(kafkaTemplate);
    }

    /**
     * Sends an AuditEvent to the designated Kafka audit topic asynchronously.
     *
     * @param event populated AuditEvent
     * @return CompletableFuture completing when message is acknowledged by Kafka
     */
    public CompletableFuture<SendResult<String, String>> sendAuditEvent(AuditEvent event) {
        if (!kafkaEnabled || kafkaTemplate.isEmpty()) {
            CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Kafka producer is disabled or unconfigured"));
            return future;
        }

        String key = event.getRequestId() != null ? event.getRequestId() : "audit-event";
        String payload = JsonUtils.toJson(event);

        log.debug("Sending audit event to topic '{}' with key '{}'", auditTopic, key);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.get().send(auditTopic, key, payload);

        future.whenComplete((result, ex) -> {
            lastDispatchTime.set(Instant.now());
            if (ex == null) {
                successfulDispatches.incrementAndGet();
                log.info("Successfully delivered audit event for requestId={} to topic={} partition={} offset={}",
                        key,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                failedDispatches.incrementAndGet();
                log.error("Failed to deliver audit event for requestId={} to Kafka topic '{}': {}", key, auditTopic, ex.getMessage());
            }
        });

        return future;
    }

    public boolean isKafkaEnabled() {
        return kafkaEnabled && kafkaTemplate.isPresent();
    }

    public long getSuccessfulDispatches() {
        return successfulDispatches.get();
    }

    public long getFailedDispatches() {
        return failedDispatches.get();
    }

    public Instant getLastDispatchTime() {
        return lastDispatchTime.get();
    }

    public String getAuditTopic() {
        return auditTopic;
    }
}
