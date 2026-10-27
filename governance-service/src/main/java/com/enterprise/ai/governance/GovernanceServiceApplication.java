package com.enterprise.ai.governance;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Enterprise AI Governance Service",
                version = "1.0.0",
                description = "Centralized Control Center for AI Policies, Model Registry, Prompt Templates, Department Quotas, and Approval Workflows",
                contact = @Contact(name = "SecOps & AI Governance Team", email = "secops-ai@enterprise.com")
        )
)
public class GovernanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovernanceServiceApplication.class, args);
    }
}
