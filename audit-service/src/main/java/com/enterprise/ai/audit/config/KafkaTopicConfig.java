package com.enterprise.ai.audit.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Defines main audit event stream and dead-letter queue topics for Audit Service.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    @Value("${app.kafka.audit-topic:ai-guardian-audit-events}")
    private String auditTopic;

    @Bean
    public NewTopic auditStreamTopic() {
        return TopicBuilder.name(auditTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditDlqTopic() {
        return TopicBuilder.name(auditTopic + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
