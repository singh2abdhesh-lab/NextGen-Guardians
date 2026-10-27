package com.enterprise.ai.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 documentation configuration for Audit Service.
 */
@Configuration
public class AuditOpenApiConfig {

    @Bean
    public OpenAPI auditServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Audit Service API")
                        .description("Enterprise AI Guardian module for immutable forensic event logging, human approval queues, executive dashboards, and regulatory compliance reports.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise AI Security Architecture Team")
                                .email("audit-ops@enterprise.ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
