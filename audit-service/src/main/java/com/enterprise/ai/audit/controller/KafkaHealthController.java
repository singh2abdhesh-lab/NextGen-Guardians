package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.consumer.KafkaAuditConsumer;
import com.enterprise.ai.audit.consumer.KafkaAuditDlqConsumer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check probe exposing Kafka consumer status, listener metrics, and DLQ tracking
 * for the Audit Service.
 */
@RestController
@RequestMapping("/api/v1/audit/kafka")
@CrossOrigin(origins = "*")
@Tag(name = "Kafka Integration", description = "Endpoints for Kafka consumer health probes, DLQ status, and ingestion metrics")
public class KafkaHealthController {

    private final KafkaAuditConsumer kafkaAuditConsumer;
    private final KafkaAuditDlqConsumer kafkaAuditDlqConsumer;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${app.kafka.audit-topic:ai-guardian-audit-events}")
    private String auditTopic;

    public KafkaHealthController(
            KafkaAuditConsumer kafkaAuditConsumer,
            KafkaAuditDlqConsumer kafkaAuditDlqConsumer) {
        this.kafkaAuditConsumer = kafkaAuditConsumer;
        this.kafkaAuditDlqConsumer = kafkaAuditDlqConsumer;
    }

    @GetMapping("/health")
    @Operation(summary = "Kafka consumer health status", description = "Returns Kafka consumer status, active topics, DLQ topic, and ingestion statistics.")
    public ResponseEntity<Map<String, Object>> kafkaHealth() {
        Map<String, Object> health = Map.of(
                "status", kafkaEnabled ? "UP" : "STANDALONE_FALLBACK_REST",
                "kafkaEnabled", kafkaEnabled,
                "auditTopic", auditTopic,
                "dlqTopic", auditTopic + ".DLT",
                "messagesReceived", kafkaAuditConsumer.getMessagesReceived(),
                "messagesProcessed", kafkaAuditConsumer.getMessagesProcessed(),
                "processingErrors", kafkaAuditConsumer.getProcessingErrors(),
                "dlqMessagesReceived", kafkaAuditDlqConsumer.getDlqMessagesReceived(),
                "lastProcessedTime", kafkaAuditConsumer.getLastProcessedTime().toString()
        );

        return ResponseEntity.ok(health);
    }
}
