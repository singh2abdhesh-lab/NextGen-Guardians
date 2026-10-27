package com.enterprise.ai.quality.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 documentation configuration for Response Quality Checker.
 */
@Configuration
public class QualityOpenApiConfig {

    @Bean
    public OpenAPI responseQualityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Response Quality Checker API")
                        .description("Enterprise AI Guardian module for hallucination detection, secret echo audits, PII leak tracking, and safety policy compliance.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise AI Security Architecture Team")
                                .email("security-ai@enterprise.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
