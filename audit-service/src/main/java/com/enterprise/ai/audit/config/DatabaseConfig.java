package com.enterprise.ai.audit.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Enterprise database and JPA configuration configuring transaction management,
 * JPA repository packages, and entity scanning.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.enterprise.ai.audit.repository")
@EntityScan(basePackages = "com.enterprise.ai.audit.entity")
public class DatabaseConfig {
    // Configures Spring Data JPA infrastructure for the audit service
}
