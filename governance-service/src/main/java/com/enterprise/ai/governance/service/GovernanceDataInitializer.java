package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initializes default enterprise governance data on boot.
 */
@Component
public class GovernanceDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GovernanceDataInitializer.class);

    private final PolicyManagementService policyService;
    private final ModelRegistryService modelService;
    private final PromptTemplateService templateService;
    private final DepartmentGovernanceService departmentService;
    private final UserGovernanceService userService;
    private final ApprovalPolicyEngineService approvalPolicyService;

    public GovernanceDataInitializer(
            PolicyManagementService policyService,
            ModelRegistryService modelService,
            PromptTemplateService templateService,
            DepartmentGovernanceService departmentService,
            UserGovernanceService userService,
            ApprovalPolicyEngineService approvalPolicyService) {
        this.policyService = policyService;
        this.modelService = modelService;
        this.templateService = templateService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.approvalPolicyService = approvalPolicyService;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing Enterprise AI Governance baseline records...");
        initPolicies();
        initModels();
        initPromptTemplates();
        initDepartments();
        initUsers();
        initApprovalPolicies();
        log.info("AI Governance baseline seeding completed successfully.");
    }

    private void initPolicies() {
        if (!policyService.getAllPolicies().isEmpty()) return;

        policyService.createPolicy(AiPolicyDto.builder()
                .policyKey("POL-SEC-001")
                .name("Prompt Injection & Jailbreak Defense")
                .category("SECURITY")
                .description("Blocks all jailbreak patterns, system prompt overrides, and adversarial evasion vectors")
                .priority(1)
                .status("ACTIVE")
                .rulesJson("{\"keywords\":[\"ignore previous instructions\",\"jailbreak\",\"system prompt\",\"DAN mode\"],\"action\":\"BLOCK\"}")
                .action("BLOCK")
                .approvalRequired(false)
                .enabled(true)
                .effectiveDate(LocalDateTime.now().minusDays(30))
                .build(), "system-init");

        policyService.createPolicy(AiPolicyDto.builder()
                .policyKey("POL-FIN-001")
                .name("Pre-Release Earnings & Forecast Firewall")
                .category("FINANCIAL")
                .description("Escalates prompts discussing unannounced earnings, salary tables, or financial forecasts to Finance Director")
                .priority(5)
                .status("ACTIVE")
                .rulesJson("{\"keywords\":[\"earnings\",\"quarterly\",\"forecast\",\"revenue\",\"salary\"],\"action\":\"REVIEW\"}")
                .action("REVIEW")
                .approvalRequired(true)
                .enabled(true)
                .effectiveDate(LocalDateTime.now().minusDays(30))
                .build(), "system-init");

        policyService.createPolicy(AiPolicyDto.builder()
                .policyKey("POL-PRIV-001")
                .name("Customer PII & Cardholder Data Redaction")
                .category("DATA_PRIVACY")
                .description("Auto-redacts SSNs, credit card numbers, and banking details before prompt ingress")
                .priority(10)
                .status("ACTIVE")
                .rulesJson("{\"keywords\":[\"ssn\",\"social security\",\"credit card\",\"passport\"],\"action\":\"REDACT\"}")
                .action("REDACT")
                .approvalRequired(false)
                .enabled(true)
                .effectiveDate(LocalDateTime.now().minusDays(30))
                .build(), "system-init");

        policyService.createPolicy(AiPolicyDto.builder()
                .policyKey("POL-CODE-001")
                .name("Private Key and Infrastructure Credential Protection")
                .category("CODE_INTEGRITY")
                .description("Blocks transmission of private cryptographic keys, AWS secrets, and database connection strings")
                .priority(2)
                .status("ACTIVE")
                .rulesJson("{\"keywords\":[\"private_key\",\"aws_secret\",\"api_key\",\"password\"],\"action\":\"BLOCK\"}")
                .action("BLOCK")
                .approvalRequired(false)
                .enabled(true)
                .effectiveDate(LocalDateTime.now().minusDays(30))
                .build(), "system-init");
    }

    private void initModels() {
        if (!modelService.getAllModels().isEmpty()) return;

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("gpt-4o")
                .vendor("OpenAI")
                .version("2024-08-06")
                .endpoint("https://api.openai.com/v1/chat/completions")
                .apiType("OPENAI_COMPATIBLE")
                .supportedFeatures("STREAMING,FUNCTION_CALLING,VISION,JSON_MODE")
                .costPer1kInput(0.0025)
                .costPer1kOutput(0.0100)
                .contextWindow(128000)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Platform AI Team")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("claude-3-5-sonnet")
                .vendor("Anthropic")
                .version("20241022")
                .endpoint("https://api.anthropic.com/v1/messages")
                .apiType("ANTHROPIC_MESSAGES")
                .supportedFeatures("STREAMING,VISION,CODE_EXECUTION")
                .costPer1kInput(0.0030)
                .costPer1kOutput(0.0150)
                .contextWindow(200000)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Platform AI Team")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("gemini-1.5-pro")
                .vendor("Google")
                .version("002")
                .endpoint("https://generativelanguage.googleapis.com/v1beta/models")
                .apiType("GEMINI_NATIVE")
                .supportedFeatures("STREAMING,AUDIO,VIDEO,1M_CONTEXT")
                .costPer1kInput(0.00125)
                .costPer1kOutput(0.0050)
                .contextWindow(1000000)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Research CoE")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("llama-3-70b")
                .vendor("Meta")
                .version("Instruct-v1")
                .endpoint("http://vllm-cluster.internal:8000/v1/completions")
                .apiType("OPENAI_COMPATIBLE")
                .supportedFeatures("STREAMING,SELF_HOSTED,ZERO_EGRESS")
                .costPer1kInput(0.0006)
                .costPer1kOutput(0.0018)
                .contextWindow(8192)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Infra Engineering")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("mistral-large")
                .vendor("Mistral")
                .version("2407")
                .endpoint("https://api.mistral.ai/v1/chat/completions")
                .apiType("OPENAI_COMPATIBLE")
                .supportedFeatures("STREAMING,FUNCTION_CALLING,JSON_MODE")
                .costPer1kInput(0.0020)
                .costPer1kOutput(0.0060)
                .contextWindow(128000)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Platform AI Team")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());

        modelService.registerModel(ModelRegistrationDto.builder()
                .modelName("internal-coder-v2")
                .vendor("Internal Models")
                .version("v2.1")
                .endpoint("http://internal-models.corp.local:8080/generate")
                .apiType("OPENAI_COMPATIBLE")
                .supportedFeatures("STREAMING,AIR_GAPPED")
                .costPer1kInput(0.0001)
                .costPer1kOutput(0.0002)
                .contextWindow(32000)
                .streamingSupport(true)
                .status("APPROVED")
                .owner("Data Science CoE")
                .healthStatus("HEALTHY")
                .enabled(true)
                .build());
    }

    private void initPromptTemplates() {
        if (!templateService.getAllTemplates().isEmpty()) return;

        templateService.createTemplate(PromptTemplateDto.builder()
                .templateKey("TMPL-FIN-001")
                .title("Executive Quarterly Financial Digest")
                .category("FINANCE")
                .description("Standard prompt for synthesizing SEC filings and quarterly balance sheets")
                .content("Act as a chartered financial analyst. Review {financial_data} for {period} and formulate a risk summary highlighting EBITDA, operating cash flow, and debt-to-equity ratio.")
                .tags("finance,earnings,ebitda,compliance")
                .isFavorite(true)
                .author("cfo-office")
                .build(), "system-init");

        templateService.createTemplate(PromptTemplateDto.builder()
                .templateKey("TMPL-ENG-001")
                .title("Secure Architecture & Code Review")
                .category("ENGINEERING")
                .description("Evaluates code snippets against OWASP Top 10 vulnerabilities and concurrency bottlenecks")
                .content("Perform a static application security audit of {code_snippet} in {language}. Flag OWASP vulnerabilities, injection vectors, and suggest modernized remediation.")
                .tags("engineering,owasp,security,code-review")
                .isFavorite(true)
                .author("secops-lead")
                .build(), "system-init");

        templateService.createTemplate(PromptTemplateDto.builder()
                .templateKey("TMPL-LEG-001")
                .title("Vendor NDA & Master Services Agreement Analysis")
                .category("LEGAL")
                .description("Extracts liability caps, indemnification, and IP ownership clauses from commercial contracts")
                .content("Analyze the legal contract excerpt: {contract_text}. Identify governing law, limitation of liability cap, data processing commitments, and mutual indemnification terms.")
                .tags("legal,contracts,nda,msa")
                .isFavorite(false)
                .author("legal-counsel")
                .build(), "system-init");
    }

    private void initDepartments() {
        if (!departmentService.getAllDepartments().isEmpty()) return;

        departmentService.saveDepartment(DepartmentGovernanceDto.builder()
                .departmentName("Engineering")
                .departmentCode("ENG")
                .dailyTokenLimit(15_000_000L)
                .monthlyCostLimitUsd(8000.0)
                .currentMonthlySpendUsd(3120.40)
                .allowedModels("gpt-4o,claude-3-5-sonnet,llama-3-70b,internal-coder-v2")
                .blockedModels("uncensored-v1")
                .allowedPromptCategories("CODE,ARCHITECTURE,DOCUMENTATION,GENERAL")
                .restrictedCategories("PAYROLL,PATIENT_HEALTH")
                .businessHoursOnly(false)
                .build());

        departmentService.saveDepartment(DepartmentGovernanceDto.builder()
                .departmentName("Finance")
                .departmentCode("FIN")
                .dailyTokenLimit(5_000_000L)
                .monthlyCostLimitUsd(3500.0)
                .currentMonthlySpendUsd(1840.60)
                .allowedModels("gpt-4o,claude-3-5-sonnet,gemini-1.5-pro")
                .blockedModels("llama-3-8b,uncensored-v1")
                .allowedPromptCategories("FINANCE,SPREADSHEET,EXECUTIVE,GENERAL")
                .restrictedCategories("SOURCE_CODE,PATIENT_HEALTH")
                .businessHoursOnly(true)
                .businessHoursStart("08:00")
                .businessHoursEnd("19:00")
                .build());

        departmentService.saveDepartment(DepartmentGovernanceDto.builder()
                .departmentName("Legal")
                .departmentCode("LEG")
                .dailyTokenLimit(4_000_000L)
                .monthlyCostLimitUsd(3000.0)
                .currentMonthlySpendUsd(1150.00)
                .allowedModels("claude-3-5-sonnet,gpt-4o,gemini-1.5-pro")
                .blockedModels("uncensored-v1")
                .allowedPromptCategories("LEGAL,CONTRACTS,COMPLIANCE,REGULATORY")
                .restrictedCategories("SOURCE_CODE")
                .businessHoursOnly(false)
                .build());
    }

    private void initUsers() {
        if (!userService.getAllUsers().isEmpty()) return;

        userService.saveUser(UserGovernanceDto.builder()
                .userId("dev.lead@enterprise.ai")
                .userRole("ENGINEER")
                .department("Engineering")
                .customDailyTokenLimit(2_000_000L)
                .isRestricted(false)
                .canBypassReview(false)
                .build());

        userService.saveUser(UserGovernanceDto.builder()
                .userId("cfo.analyst@enterprise.ai")
                .userRole("ANALYST")
                .department("Finance")
                .customDailyTokenLimit(500_000L)
                .isRestricted(false)
                .canBypassReview(false)
                .build());

        userService.saveUser(UserGovernanceDto.builder()
                .userId("contractor.external@enterprise.ai")
                .userRole("CONTRACTOR")
                .department("Engineering")
                .customDailyTokenLimit(100_000L)
                .isRestricted(false)
                .restrictionReason("External contractor: full oversight gating required")
                .canBypassReview(false)
                .build());
    }

    private void initApprovalPolicies() {
        if (!approvalPolicyService.getAllApprovalPolicies().isEmpty()) return;

        approvalPolicyService.saveApprovalPolicy(ApprovalPolicyDto.builder()
                .policyKey("APPR-HIGH-RISK")
                .name("High Risk Escalation Workflow")
                .triggerCategory("HIGH_RISK")
                .workflowType("ESCALATION")
                .primaryApproverRole("SECURITY_LEAD")
                .escalationApproverRole("CISO")
                .autoExpiryHours(24)
                .description("Triggers dual-tier escalation to CISO if prompt risk score exceeds 75")
                .enabled(true)
                .build());

        approvalPolicyService.saveApprovalPolicy(ApprovalPolicyDto.builder()
                .policyKey("APPR-FIN-01")
                .name("Corporate Financial & Earnings Review")
                .triggerCategory("FINANCE")
                .workflowType("MULTI_LEVEL")
                .primaryApproverRole("FINANCE_DIRECTOR")
                .escalationApproverRole("CFO")
                .autoExpiryHours(12)
                .description("Mandatory multi-level approval for material non-public financial calculations")
                .enabled(true)
                .build());

        approvalPolicyService.saveApprovalPolicy(ApprovalPolicyDto.builder()
                .policyKey("APPR-LEG-01")
                .name("Legal & Regulatory Review")
                .triggerCategory("LEGAL")
                .workflowType("SINGLE_APPROVAL")
                .primaryApproverRole("LEGAL_COUNSEL")
                .autoExpiryHours(48)
                .description("Sign-off for contractual queries covered under customer NDAs")
                .enabled(true)
                .build());

        approvalPolicyService.saveApprovalPolicy(ApprovalPolicyDto.builder()
                .policyKey("APPR-SEC-01")
                .name("Critical Secrets Review")
                .triggerCategory("CRITICAL_SECRETS")
                .workflowType("PARALLEL")
                .primaryApproverRole("SECURITY_LEAD")
                .escalationApproverRole("DEVOPS_LEAD")
                .autoExpiryHours(6)
                .description("Parallel security and infrastructure verification when token leak is suspected")
                .enabled(true)
                .build());
    }
}
