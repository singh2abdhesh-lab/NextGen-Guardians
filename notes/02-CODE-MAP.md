# Code map — what the services actually do vs deck claims

Verified against the working tree (branch `NextGen-Guardians-dev`) on 2026-10-27 by a full codebase
exploration pass. Every "deck claim" below comes from `01-IDEA-AND-DECKS.md`. File paths are relative
to repo root; line anchors are where the important values live.

## Stack & infra (real)

- Root `pom.xml`: Spring Boot **3.3.5**, Java 17, 7 modules (common, prompt-security-scanner,
  risk-scoring-engine, audit-service, response-quality-checker, ai-gateway, governance-service),
  springdoc 2.6.0, Jacoco.
- `docker-compose.yml`: Postgres 16 (`ai_guardian_audit`), Zookeeper + Kafka (confluent 7.6.0),
  plus all 7 app containers (8080–8085 + frontend :3000). `AI_UPSTREAM_MOCK` defaults true.
- `application.yml` defaults: mock LLM on, **kafka.enabled=false** (REST fallback to audit-service),
  all services on localhost.

## Service-by-service reality

### 1. ai-gateway (8080, WebFlux)

- **Pipeline is 9 stages, not 8** — `service/GatewayPipelineService.java`:
  INTERCEPT(74) → IDENTITY(77) → SCAN(112) → REDACT(120, optional auto-redact + re-scan) →
  RISK(173) → DECISION(190) → LLM(212) → QUALITY(220) → AUDIT(290). The deck says "8-stage" on
  slides 3/5 and "9-stage trace" on slides 4/17 — code says 9. Blocked stages get SKIPPED trace
  records (`pipeline/PipelineTracer.java` maps stage→owning service).
- **DecisionEngine** (`engine/DecisionEngine.java`) has **no numeric cutoffs itself** — it consumes
  the risk engine's action. Prompt gate: scan-blocked or risk-BLOCK→BLOCK; risk REVIEW→REVIEW
  (approvalRequired=true, request still continues to LLM). Post-completion gate: quality
  policyViolation/FAIL→REVIEW, else ALLOWED (actions ALLOWED/BLOCKED/REVIEW, lines 106–116).
- **LlmClient** (`client/LlmClient.java`): ONE OpenAI-compatible client (`/v1/chat/completions`,
  max_tokens 1000, temp 0.7) + mock mode. Circuit breaker (`llm/LlmCircuitBreaker.java`: failure
  threshold 3, reset 15s, CLOSED/OPEN/HALF_OPEN), retry 3×/500ms on 429/5xx/timeout, graceful
  `[DEGRADED_FALLBACK]`/`[CIRCUIT_BREAKER_FALLBACK]` mock output. **Claude/Gemini exist only as
  pricing-table entries — not as integrations.**
- **Token/Cost**: `llm/TokenCalculator.java` regex heuristic (~4 chars/token, +3 per message);
  `llm/CostCalculator.java:32-63` per-1k pricing — gpt-4o 0.0025/0.01 (default, yml:30-31),
  gpt-4o-mini 0.00015/0.0006, claude-3-5-sonnet 0.003/0.015, claude-3-haiku 0.00025/0.00125,
  gemini-1.5-pro 0.00125/0.005, gemini-1.5-flash 0.000075/0.0003.
- **Security stack**:
  - `security/JwtTokenService.java`: hand-rolled HS256 (no library), **hardcoded default secret**
    `ai-guardian-enterprise-security-jwt-secret-key-super-secure-32bytes` (line 29), 3600s validity.
  - `security/RbacSecurityManager.java:20-65`: INTERN denied FINANCE + high-tier models; helpers
    `canApproveReviewQueue` etc. exist but are **not wired to any REST endpoint**.
  - `security/RateLimitingService.java:60-68`: fixed 60s in-memory window; ADMIN/SECURITY 300,
    ENGINEER 100, ANALYST 60, INTERN 20 req/min. Enforced via `config/SecurityWebFilter`.
  - `security/SecurityPayloadValidator`: 32k-char cap + invisible-unicode strip — **not injected
    into the pipeline**; GatewayPipelineService inlines the same checks (lines 92–101).
- **Audit publish** (`publisher/AuditPublisher.java`): Kafka if enabled, else REST fallback to
  audit-service `/api/v1/audit/events`; Kafka failure also falls back to REST. Topic
  `ai-guardian-audit-events` + `.DLT` dead-letter (`config/KafkaTopicConfig.java`). AuditEvent
  fields (GatewayPipelineService.java:269–288): requestId, userId, userRole, model, promptCategory,
  action, riskScore, riskLevel, tokensIn/out, costUsd, qualityVerdict, hallucinationScore,
  approvalRequired, violationsSummary, blockReason, prompt, timestamp.
- **Endpoints** (`controller/GatewayController.java`): POST `/api/v1/ai/chat` + `/api/v1/gateway/chat`
  (full pipeline, line 66); **POST `/api/v1/ai/chat/stream` (line 96) BYPASSES the pipeline** and
  streams LLM directly — a governance hole; POST `/api/v1/auth/token` (line 114) issues a JWT for
  **any userId/role, unauthenticated**; health endpoints; Swagger at `/swagger-ui.html`.

### 2. prompt-security-scanner (8081)

- **Pure regex** — no ML, no "behavioral heuristics" (deck slide 6 claim overshoots).
- Detectors (`detector/`): PiiDetector (SSN, card w/ Luhn prefixes, email, phone); SecretDetector
  (sk-, AKIA, aws_secret, ghp_/github_pat_, xox, PEM); CredentialDetector (JWT bearer, Basic auth,
  jdbc/mongo/redis conn strings, password=); PromptInjectionDetector + MaliciousPromptDetector
  (overlapping duplicates: "ignore previous instructions", DAN/sudo/god mode, reveal system prompt,
  `<system>` tags, SQLi); ConfidentialDataDetector + older ConfidentialDetector (**both active —
  duplicates**); SourceCodeDetector.
- Score: severity-weight sum → sensitivityScore capped 100; **blocked = any CRITICAL violation OR
  sensitivity ≥ 70** (`service/ScannerService.java:58-63`).
- Endpoints (`controller/ScannerController.java`): POST `/api/v1/scan`, POST `/api/v1/scan/redact`,
  health. Redaction masks (`RedactionEngine.java:62-83`): `[REDACTED]`, `[REDACTED_SSN]`,
  `[REDACTED_CARD]`, `[REDACTED_EMAIL]`, `[REDACTED_API_KEY]`, `[REDACTED_CREDENTIAL]`,
  `[REDACTED_CONFIDENTIAL]`.

### 3. risk-scoring-engine (8082)

- **Weights do NOT match the deck's 35/20/15/20/10 five-factor matrix.** Actual
  (`common/.../constant/SecurityConstants.java:15-18`): **dataSensitivity 0.40, userRole 0.25,
  promptCategory 0.20, targetModel 0.15 — four factors**; no department factor, no separate intent
  factor (category ≈ intent).
- Scorers (`risk/engine/`): DataSensitivityFactorScorer (floor 75 for MALICIOUS/SECRET/INJECTION),
  UserRoleFactorScorer, PromptCategoryFactorScorer, TargetModelFactorScorer. Base scores: roles
  ADMIN/SECURITY 15, ENGINEER/ANALYST 45, INTERN 80; categories GENERAL 20, CODE 65, FINANCE 75,
  HR 70, LEGAL 60, HEALTHCARE 80, MARKETING 30.
- Bands (`RiskCalculator.java:40-48`): LOW <30, MEDIUM ≥30, HIGH ≥60, CRITICAL ≥80 — deck tiers
  (0-29/30-59/60-79/80-100) match this.
- Actions (`RiskRuleEngine.java:18-40`): BLOCK if CRITICAL **or dataSensitivity ≥ 70**; REVIEW if
  INTERN+FINANCE/CODE with score ≥50; REVIEW if HIGH; else ALLOW. So the 60/80 "decision thresholds"
  the deck pitches are actually here, expressed as bands + sensitivity gate.
- Endpoint: POST `/api/v1/risk/assess` (+ `/score` alias), health.

### 4. response-quality-checker (8084)

- Checkers (`checker/`): HallucinationDetector — **additive regex scoring**, not "statistical
  confidence": fabricated URL +0.40, suspicious citation +0.30, overconfidence ("guaranteed",
  "100% accurate") +0.25, invented regulation +0.35, cap 1.0 (HallucinationDetector.java:55-67).
  SecretEchoDetector (response echoes sk-/AKIA/ghp_/xox/PEM/JWT/passwords), PiiLeakageDetector,
  PolicyChecker (jailbreak affirmation, system-prompt leakage, unauthorized med/legal/financial
  advice, toxicity).
- Verdicts (`QualityScoreCalculator.java:53-59`): any violation OR hallucination ≥0.70 → FAIL;
  ≥0.30 → REVIEW; else PASS. **Inconsistency:** SecurityConstants defines
  MAX_TOLERABLE_HALLUCINATION_SCORE=0.50 / TOXICITY=0.35 but the calculator hardcodes its own 0.70/0.30.
- Endpoint: POST `/api/v1/quality/check`.

### 5. audit-service (8083)

- `KafkaAuditConsumer` (@KafkaListener, group audit-service-group) + `KafkaAuditDlqConsumer` on
  `.DLT`; **auto-startup defaults false** (`KAFKA_AUTO_STARTUP`) — Kafka mode only under docker.
- Persistence: **default H2 in-memory** (`jdbc:h2:mem:auditdb MODE=PostgreSQL`) → audit data is
  LOST on restart outside docker; Postgres only via docker-compose env.
- Entities: AuditEventEntity (+ approvalStatus/approver/approvalTimestamp/approvalReason, 6 indexes),
  ViolationEntity (1-many), ApprovalRecordEntity, ComplianceSnapshotEntity, ModelPricingEntity.
- Endpoints: `AuditController` — ingest (`/api/v1/audit/events`, `/batch`), query (`/events`,
  `/{id}`, `/request/{requestId}`, `/user/{userId}`), `/alerts` (HIGH/CRITICAL), **`/pending` +
  `/{id}/approve` + `/{id}/reject` — the real human-in-the-loop approval queue lives HERE**, not in
  governance-service. `DashboardController` (`/api/v1/dashboard`: overview, usage/models,
  usage/users, categories, compliance). `ComplianceController` (`/report`, `/export/csv`,
  `/frameworks` — **hardcoded list** GDPR/EU AI Act etc., `/audit-trail/{requestId}`).

### 6. governance-service (8085)

- Full CRUD store, 7 entities. Controllers under `/api/v1/governance`: `/policies` (incl. versioning
  + `/rollback` + `/history`), `/approvals` (approval **policies** = rules for when approval is
  needed — NOT the queue), `/models` (+ health-check), `/departments` (+ emergency-override,
  `/spend`), `/templates` (list/favorites/search), `/simulator/run` (what-if policy simulation),
  `/overview` + `/notifications` (in-memory 50-item ring buffer, log-only — no email/Slack).
- Seeds 4 policies + ~6 models at startup (`GovernanceDataInitializer`).
- **Critical gap: policies stored here are NOT consumed by the gateway.** DecisionEngine /
  RiskRuleEngine thresholds are compiled constants. "Policy hot-reload / centralized enforcement"
  (deck slides 9/16) is storage + simulation only.

### 7. common module

DTOs (ChatRequest/Response, ScanRequest/Result, RiskAssessRequest/RiskScore, QualityCheckRequest/
Result, AuditEvent, ApprovalRequest, DashboardOverview…), enums (UserRole, RiskLevel, PromptCategory,
QualityVerdict, ViolationType w/ weights, EnforcementAction, ApprovalStatus), pipeline trace model
(PipelineContext/Stage/Result), `AesEncryptionService` (AES-256-GCM — **has tests, no production
caller**), SanitizationUtils, TokenEstimatorUtils, SecurityConstants (all weights/thresholds).

### 8. frontend (React 18 + Vite, port 3000)

- `App.jsx` views: DashboardView, PlaygroundView (demo console, generates events), AuditLogView,
  **ApprovalsView (approve/reject pending — wired to audit-service)**, ComplianceView,
  SystemHealthView, EventDetailModal + **PipelineTraceViewer (renders the 9-stage trace)**.
- Full governance suite (`components/governance/`): GovernanceOverview, Policies, ModelRegistry,
  PromptTemplates, DepartmentGovernance, UserGovernance, ApprovalPolicies, PolicyHistory,
  PolicySimulator.
- `services/api.js` (+ governanceApi.js), 20s polling, JWT from `/api/v1/auth/token` (see
  00-REPO-STATE.md for the past session's `_live`/fallback changes here).

## Deck-claim verdict table

| Deck claim | Code reality | Verdict |
|---|---|---|
| 8-stage pipeline | 9 stages (REDACT + DECISION split out) | ❌ deck wrong, code better |
| Risk weights 35/20/15/20/10, 5 factors | 40/25/20/15, 4 factors, no department factor | ❌ mismatch |
| Risk tiers 0-29/30-59/60-79/80-100 | LOW<30, MEDIUM≥30, HIGH≥60, CRITICAL≥80 | ✅ matches |
| ALLOW <60 / REVIEW 60-79 / BLOCK ≥80 | BLOCK = CRITICAL or dataSens ≥70; REVIEW = HIGH or INTERN+FIN/CODE ≥50 | ⚠️ roughly equivalent, but mechanism differs |
| Auto-redaction [REDACTED_*] | Real, 7 mask types, re-scan after redact | ✅ |
| Multi-vendor LLM (OpenAI/Claude/Gemini) | Single OpenAI-compatible client + mock; others = pricing rows only | ❌ overstated |
| Deterministic mock provider | Real, default on, fallback-tagged | ✅ |
| Token + cost metering | Real (heuristic tokenizer, per-1k pricing table) | ✅ |
| Hallucination "statistical confidence scoring" | Additive regex heuristics | ❌ overstated, works though |
| Ingress + egress inspection | Real (scanner + quality checker) | ✅ |
| Kafka audit ledger | Real, but kafka.enabled=false by default; REST fallback | ✅ w/ caveat |
| Tamper-proof / immutable ledger | H2 in-memory by default — data lost on restart; no tamper evidence | ❌ "tamper-evident" unsupported |
| Approval queue (shown on slide 15 as current) | Real, in audit-service, frontend ApprovalsView wired | ✅ (deck roadmap says Phase 2 — it's already done) |
| Policy hot-reload, centralized policy enforcement | Policies are CRUD + simulation; gateway thresholds hardcoded | ❌ not enforced |
| Model registry, dept governance, prompt templates | CRUD + simulator, not wired into gateway decisions | ⚠️ UI/CRUD only |
| RBAC clearance checks | Implemented for identity path; approve/audit/policy endpoints don't check roles | ⚠️ partial |
| Prometheus metrics, 99.99% uptime, <45ms, 99.8% accuracy | No evidence in code; unmeasured marketing numbers | ❌ don't repeat to jury |
| NIST/ISO/OWASP compliance mapping | `/compliance/frameworks` is a hardcoded list; audit fields exist | ⚠️ cosmetic |
| HSM, multi-region (Phase 3) | Absent (fine — roadmap) | n/a |

## Security issues to know before the jury asks

1. `/api/v1/ai/chat/stream` bypasses the whole pipeline (GatewayController.java:96).
2. `/api/v1/auth/token` mints unlimited JWTs for any claimed role, no auth.
3. Hardcoded default JWT secret in JwtTokenService (would also trip the rubric's committed-secrets gate).
4. Approval endpoints don't enforce `canApproveReviewQueue` RBAC.
5. Rate limiting is per-instance in-memory (resets on restart, no distributed state).

## Cleanup candidates (duplicate/legacy)

- ConfidentialDetector vs ConfidentialDataDetector — both active in detector chain.
- MaliciousPromptDetector vs PromptInjectionDetector — overlapping.
- SecurityPayloadValidator not wired into pipeline (logic duplicated inline).
- AesEncryptionService unused in production code.
- QualityScoreCalculator ignores SecurityConstants thresholds (0.50/0.35 vs 0.70/0.30).
