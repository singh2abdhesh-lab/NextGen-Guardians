package com.enterprise.ai.gateway.publisher;

import com.enterprise.ai.common.dto.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Asynchronously publishes immutable audit events to Kafka with automatic,
 * zero-data-loss fallback to the audit-service REST endpoint.
 */
@Component
public class AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditPublisher.class);

    private final KafkaAuditProducer kafkaAuditProducer;
    private final WebClient auditClient;

    @Autowired
    public AuditPublisher(
            KafkaAuditProducer kafkaAuditProducer,
            @Qualifier("auditClient") WebClient auditClient) {
        this.kafkaAuditProducer = kafkaAuditProducer;
        this.auditClient = auditClient;
    }

    public AuditPublisher(
            WebClient auditClient,
            KafkaTemplate<String, String> kafkaTemplate) {
        this(new KafkaAuditProducer(kafkaTemplate), auditClient);
    }

    /**
     * Publishes an audit event asynchronously on a background scheduler.
     *
     * @param event populated AuditEvent
     * @return Mono<Void> completing immediately without blocking client responses
     */
    public Mono<Void> publish(AuditEvent event) {
        if (event == null) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            if (kafkaAuditProducer.isKafkaEnabled()) {
                kafkaAuditProducer.sendAuditEvent(event).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Kafka async publish failed for requestId={}; executing REST fallback: {}",
                                event.getRequestId(), ex.getMessage());
                        publishViaRest(event);
                    }
                });
            } else {
                publishViaRest(event);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private void publishViaRest(AuditEvent event) {
        try {
            auditClient.post()
                    .uri("/api/v1/audit/events")
                    .bodyValue(event)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .doOnError(e -> log.error("REST audit fallback delivery failed for requestId={}: {}", event.getRequestId(), e.getMessage()))
                    .subscribe();
            log.debug("Dispatched audit event via REST fallback for requestId={}", event.getRequestId());
        } catch (Exception e) {
            log.error("Fatal: failed to dispatch audit event via REST fallback: {}", e.getMessage());
        }
    }
}
