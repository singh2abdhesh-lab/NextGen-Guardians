# The Idea — Enterprise AI Guardian

Source: `SDS_Enterprise_AI_Guardian_Presentation.pptx` (21 slides, main deck) and
`SDS_Enterprise_AI_Guardian_Executive_Pitch.pptx` (same 21 slides + speaker "PITCH CUE" notes for a
10-minute jury pitch). Extracted 2026-10-27 from raw slide XML.

## One-liner

**Enterprise AI Guardian** — a centralized, zero-bypass **inline AI gateway + governance
observability platform**: every enterprise prompt to any LLM passes through a proxy that scans it for
secrets/PII/injection, scores risk 0–100, decides ALLOW / REVIEW / BLOCK, redacts sensitive tokens,
forwards to the model, then checks the *response* for hallucination/leakage/toxicity, and logs
everything to an immutable audit ledger. Track: **AI Governance & Security** (Digithon '26).

Submitted by: Abdhesh Singh, Dept — Knox Part. (Team NextGen-Guardians.)

## The problem it claims to solve

Uncontrolled GenAI adoption → data exfiltration. Five vectors named:
1. Credentials in prompts (API keys, JWTs, AWS creds, DB passwords)
2. Customer PII (SSN, credit cards, emails, patient records)
3. Proprietary source code / trade secrets
4. Confidential documents (financials, M&A, NDA material)
5. Prompt injection / jailbreak attacks

Core imperative (deck quote): "Organizations cannot ban AI without stalling innovation. They must
enforce visibility, sanitization, and policy before requests exit the perimeter."

## Claimed architecture (deck slides 3–5)

Eight-step data flow:
```
Employee prompt → AI Gateway (proxy) → Security Scanner (PII/injection)
→ Risk Engine (score 0–100) → Decision Engine (ALLOW/REVIEW/BLOCK)
→ LLM (frontier or mock) → Quality Checker (hallucination/safety)
→ Audit Dashboard (Kafka + H2 ledger)
```

Six services:
| Service | Port | Role per deck |
|---|---|---|
| AI Gateway | 8080 | Reactive WebClient reverse proxy, 9-stage pipeline trace, circuit breaker |
| Prompt Security Scanner | 8081 | Regex/pattern detection: keys, passwords, PII, jailbreaks, confidential code |
| Risk Scoring Engine | 8082 | Weighted multi-factor composite risk score 0–100 |
| LLM Connector | — | OpenAI/Claude/Gemini adapter + deterministic mock provider |
| Response Quality Checker | 8084 | Hallucination, secret regurgitation, toxicity, PASS/FAIL verdict |
| Audit Service | 8083 | Kafka streaming + H2/Postgres persistence, compliance reporting |

## Key claimed mechanics

- **Risk weights** (slide 8): Data Sensitivity 35% · User Role/Clearance 20% · Prompt Intent 20% ·
  Department Governance 15% · Target Model Profile 10%.
- **Risk tiers**: LOW 0–29 · MEDIUM 30–59 · HIGH 60–79 · CRITICAL 80–100.
- **Decision gates** (slide 9): ALLOW <60 · REVIEW 60–79 (human-in-the-loop SecOps queue) ·
  BLOCK ≥80 (jailbreak / unredactable secrets).
- **Auto-redaction** (slide 7): block-vs-sanitize differentiation — SSN/card/API key/password get
  masked as `[REDACTED_*]` while preserving semantic utility, instead of rejecting the prompt.
- **FinOps** (slide 10): per-request token accounting + dollar cost metering against vendor pricing
  ($5/1M input, $15/1M output cited); deterministic mock provider for zero-cost local runs.
- **Egress inspection** (slide 11): both ingress AND egress checked ("Key Differentiator" pitch cue).
- **Audit metadata** (slide 12): user identity/role/IP, department cost center, prompt category,
  detected entities, risk score, verdict, tokens, cost, latency, OWASP LLM Top-10 mapping.
- **Governance admin** (slide 16): policy management with hot-reload, model registry, prompt
  templates, per-department quotas/budgets, approval rules matrix.
- **Compliance framing**: NIST AI RMF, ISO/IEC 42001, OWASP Top 10 for LLMs.

## Business impact claims (slide 18)

- Security & IP defense: "100% prevention of critical IP leaks"
- Compliance: audit-ready in <5 min
- Cost: "30–40% reduction in runaway AI bills" (quotas + smart routing)
- Adoption: security teams can say yes to AI

## Roadmap (slide 20)

- Phase 1 (current): gateway + scanner + risk engine + redaction + audit logging
- Phase 2 (Q3–Q4 '26): dept quotas/budgets, fine-grained RBAC, approval queue, prompt templates, compliance report export
- Phase 3 (2027): K8s multi-region, self-learning detectors, HSM vault, agentic verification, federated threat sharing

## Deck provenance & unfinished state

Both `.pptx` files are **generated** by `generate_deck.py` (python-pptx, ~1540 lines, in repo root) —
same script produced both; the exec pitch just adds "PITCH CUE" lines in footers. Consequences:
- To change deck content, edit the script and regenerate (needs python + python-pptx).
- **Every product screenshot on slides 6–15 is still a placeholder** —
  `[ APPLICATION SCREENSHOT REQUIRED ] Drop / Paste verified application screenshot here`. The deck
  is NOT presentation-ready; screenshots of the running app must be captured (chrome-devtools MCP +
  the digithon-browser skill can do this) and either pasted into the PPTX or added to the script.
- Unverifiable marketing numbers to be careful about with the jury: "99.8% detection accuracy",
  "<45ms latency overhead", "99.99% uptime design", "40%+ shadow AI usage", "100% IP-leak prevention".

## Pitch-delivery notes (speaker cues embedded in the exec deck)

- 30-second problem opener; blanket-bans-fail argument.
- "Guardian sanitizes and permits work to continue" vs firewalls that only block.
- Demo beats: redaction side-by-side, risk gauge, decision verdict, audit export, mock provider
  ("judges love zero-cost local testing").
- Thresholds are claimed hot-configurable per department.

## Deck claims to verify against code (honesty audit TODO)

See `02-CODE-MAP.md` for what's actually implemented. Spot-check list:
- [x] Is the pipeline 8 or 9 stages? Deck says both in different slides. → **9 stages** (REDACT + DECISION split out). Code is right; deck slides 3/5 say 8, slides 4/17 say 9.
- [x] Do risk weights actually match 35/20/15/20/10? → **No. Code: 40/25/20/15, 4 factors, no department factor.**
- [x] Are thresholds 60/80 real and configurable? → Bands match (HIGH≥60, CRITICAL≥80) but thresholds are **compiled constants, not configurable**, and BLOCK also fires at dataSensitivity ≥70.
- [x] Does the approval queue exist? (deck lists it as Phase 2 yet shows screenshots) → **It exists and works** — in audit-service `/pending` + approve/reject, wired to frontend ApprovalsView. Deck undersells it.
- [x] Hot-reload of policies — real or decorative? → **Decorative.** Governance-service stores/simulates policies; gateway thresholds are hardcoded constants.
- [x] Multi-vendor LLM support vs mock-only? → **Single OpenAI-compatible client + mock.** Claude/Gemini are pricing-table rows only.
- [x] "99.8% detection accuracy", "<45ms latency", "99.99% uptime design" — measured or invented? → **No evidence in code. Don't cite to jury.**
- [x] Prometheus metrics — real actuator endpoints? → Spring Actuator via Boot; Prometheus-format scraping not evidenced. Full verdict table in `02-CODE-MAP.md`.
