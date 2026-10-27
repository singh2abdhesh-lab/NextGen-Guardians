package com.enterprise.ai.gateway.controller;

import com.enterprise.ai.gateway.publisher.KafkaAuditProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = KafkaHealthController.class)
@DisplayName("Kafka Health Controller WebFlux Tests")
class KafkaHealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private KafkaAuditProducer kafkaAuditProducer;

    @Test
    @DisplayName("GET /api/v1/gateway/kafka/health returns producer metrics")
    void testKafkaHealthEndpoint() {
        when(kafkaAuditProducer.isKafkaEnabled()).thenReturn(true);
        when(kafkaAuditProducer.getAuditTopic()).thenReturn("ai-guardian-audit-events");
        when(kafkaAuditProducer.getSuccessfulDispatches()).thenReturn(42L);
        when(kafkaAuditProducer.getFailedDispatches()).thenReturn(1L);
        when(kafkaAuditProducer.getLastDispatchTime()).thenReturn(Instant.now());

        webTestClient.get()
                .uri("/api/v1/gateway/kafka/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.kafkaEnabled").isEqualTo(true)
                .jsonPath("$.topic").isEqualTo("ai-guardian-audit-events")
                .jsonPath("$.successfulDispatches").isEqualTo(42);
    }
}
