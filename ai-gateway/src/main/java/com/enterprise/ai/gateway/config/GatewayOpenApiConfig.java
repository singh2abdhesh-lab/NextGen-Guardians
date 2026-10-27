package com.enterprise.ai.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 specification configuration for AI Gateway.
 */
@Configuration
public class GatewayOpenApiConfig {

    @Bean
    public OpenAPI aiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise AI Gateway API")
                        .description("Central ingress proxy governing LLM prompts and completions through multi-stage security, risk scoring, response verification, and forensic audit tracing.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise AI Security Architecture Team")
                                .email("gateway-core@enterprise.ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

