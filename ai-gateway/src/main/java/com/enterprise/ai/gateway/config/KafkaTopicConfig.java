package com.enterprise.ai.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Defines Kafka topics for AI Guardian audit event streams and dead letter queues.
 */
@Configuration
@ConditionalOnProperty(name = "ai.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    @Value("${ai.kafka.topic:ai-guardian-audit-events}")
    private String auditTopic;

    @Bean
    public NewTopic aiGuardianAuditTopic() {
        return TopicBuilder.name(auditTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic aiGuardianAuditDlqTopic() {
        return TopicBuilder.name(auditTopic + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

