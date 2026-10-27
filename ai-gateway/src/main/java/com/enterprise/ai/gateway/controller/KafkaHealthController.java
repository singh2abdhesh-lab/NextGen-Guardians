package com.enterprise.ai.gateway.controller;

import com.enterprise.ai.gateway.publisher.KafkaAuditProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Health check probe exposing Kafka producer connectivity, topic configuration,
 * and delivery metrics for the AI Gateway.
 */
@RestController
@RequestMapping("/api/v1/gateway/kafka")
@CrossOrigin(origins = "*")
@Tag(name = "Kafka Integration", description = "Endpoints for Kafka producer health probes, topic status, and message metrics")
public class KafkaHealthController {

    private final KafkaAuditProducer kafkaAuditProducer;

    public KafkaHealthController(KafkaAuditProducer kafkaAuditProducer) {
        this.kafkaAuditProducer = kafkaAuditProducer;
    }

    @GetMapping("/health")
    @Operation(summary = "Kafka producer health status", description = "Returns Kafka producer status, target topic, dispatched message counts, and fallback mode.")
    public Mono<ResponseEntity<Map<String, Object>>> kafkaHealth() {
        boolean enabled = kafkaAuditProducer.isKafkaEnabled();

        Map<String, Object> health = Map.of(
                "status", enabled ? "UP" : "STANDALONE_FALLBACK_REST",
                "kafkaEnabled", enabled,
                "topic", kafkaAuditProducer.getAuditTopic(),
                "successfulDispatches", kafkaAuditProducer.getSuccessfulDispatches(),
                "failedDispatches", kafkaAuditProducer.getFailedDispatches(),
                "lastDispatchTime", kafkaAuditProducer.getLastDispatchTime().toString()
        );

        return Mono.just(ResponseEntity.ok(health));
    }
}

