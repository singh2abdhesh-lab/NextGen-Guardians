package com.enterprise.ai.audit.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Enterprise Kafka Consumer configuration featuring automatic exponential retry backoff
 * and Dead Letter Queue (DLQ) recovery for poison-pill or failed event payloads.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${app.kafka.audit-topic:ai-guardian-audit-events}")
    private String auditTopic;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    /**
     * Configures the Kafka listener container factory with exponential retries and DLQ recoverer.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setAutoStartup(kafkaEnabled);

        if (kafkaTemplate != null) {
            // Dead Letter Publishing Recoverer routes exhausted messages to <topic>.DLT
            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                    (KafkaOperations<String, String>) kafkaTemplate,
                    (record, ex) -> {
                        log.error("Exhausted retries for record with key={} on topic={}. Routing to DLQ: {}",
                                record.key(), record.topic(), ex.getMessage());
                        return new TopicPartition(auditTopic + ".DLT", record.partition());
                    }
            );

            // Exponential backoff: 3 retry attempts (1s, 2s, 4s)
            ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
            backOff.setMaxElapsedTime(7000L);

            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
            errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                    log.warn("Retrying delivery attempt #{} for audit record key={} due to: {}",
                            deliveryAttempt, record.key(), ex.getMessage()));

            factory.setCommonErrorHandler(errorHandler);
        }

        return factory;
    }
}
