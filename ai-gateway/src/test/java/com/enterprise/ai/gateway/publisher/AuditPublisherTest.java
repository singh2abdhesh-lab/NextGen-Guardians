package com.enterprise.ai.gateway.publisher;

import com.enterprise.ai.common.dto.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Publisher Unit Tests")
class AuditPublisherTest {

    @Mock
    private WebClient auditClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private AuditPublisher auditPublisher;

    @BeforeEach
    void setUp() {
        auditPublisher = new AuditPublisher(auditClient, kafkaTemplate);
    }

    @Test
    @DisplayName("Publish should handle null event gracefully")
    void testPublishNullEvent() {
        StepVerifier.create(auditPublisher.publish(null))
                .verifyComplete();

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Publish should complete asynchronously without blocking")
    void testPublishValidEvent() {
        AuditEvent event = AuditEvent.builder()
                .requestId("req-pub-1")
                .userId("tester")
                .build();

        StepVerifier.create(auditPublisher.publish(event))
                .verifyComplete();
    }
}

