# Repo & session state — NextGen-Guardians

Last updated: 2026-10-27 (evening session)

## Branch situation

- Local checkout is on **`NextGen-Guardians-dev`**, which is exactly `origin/development` + one commit
  `f4b7aac "Initial changes"` (that commit adds the entire codebase, ~41k lines across 304 files).
- `origin/development` and `origin/main` both point at `1dc34cf "Create Readme.md"` — i.e. the
  remote is essentially empty except for a README.
- Other remote branches exist (`feature/*`, `feature-nextgen-guardians`) but all contain only
  `1dc34cf` — no diverged work there.
- **Conclusion:** `NextGen-Guardians-dev` IS the development branch for this team's work; there is no
  separate `development` branch content to switch to. The user asked to "go to development branch" —
  the current branch is already the de-facto development branch.

## Uncommitted changes (from past session, ~2026-10-27 ~16:40–19:15)

49 modified files, all unstaged. Two themes — they look like fixes for real issues hit while running
the stack:

### 1. Spring bean qualifier renames (ai-gateway)
`WebClientConfig` bean names renamed `scannerClient → scannerWebClient`, `riskClient → riskWebClient`,
`auditClient → auditWebClient`, `qualityClient → qualityWebClient`, `llmClient → llmWebClient`, with
matching `@Qualifier` updates in `LlmClient`, `QualityClient`, `RiskClient`, `ScannerClient`,
`AuditPublisher`. Reason (inferred): bean-name/field-name collisions caused ambiguous injection or
confusing startup errors — `llmClient` bean name vs `LlmClient` class being a classic trap.

Also: `LlmCircuitBreaker` constructor got an explicit `@Autowired` (lines ~30) and
`import org.springframework.beans.factory.annotation.Autowired` added.

### 2. Frontend live-vs-simulation honesty fix
- `frontend/src/services/api.js`: added `ensureToken()` — fetches a JWT from `POST /api/v1/auth/token`
  with `{userId: 'secops-operator', role: 'ANALYST'}` and attaches `Authorization: Bearer` to gateway
  calls; every live response is tagged `_live: true`; simulation fallbacks carry no `_live` (so
  `_live === false` distinguishes them when explicitly merged).
- `frontend/src/components/PlaygroundView.jsx`: `userId` changed from an email
  (`secops.operator@enterprise.ai`) to `secops-operator` (dot-free — matches what the JWT endpoint
  expects); result now records `_live`; an amber banner renders when `result._live === false`:
  "⚠ Simulated result — backend unreachable. Showing client-side pipeline emulation, not live gateway data."
- `frontend/vite.config.js`: added dev-proxy rule `/api/v1/auth` → `http://localhost:8080`.

### 3. Cosmetic
Nearly every touched Java file just gained a trailing blank line at EOF (editor artifact, harmless).

## How to run the stack

- `start-all.ps1` / `start-all.bat` (and `.sh` variants) — starts the 5 backend services + frontend.
- `check-health.ps1` — service health probe script.
- `docker-compose.yml` present, but deck claims "zero external infra needed" (H2 embedded mode).

## Related but separate: `D:\digithon\nexgen-guardrails`

A **different** project built earlier today (2026-10-27, committed locally, git repo): a Go
"guardrails proxy" with its own DESIGN.md, README, todo.md (last touched 21:43). Regex-based PII
redaction, caching, budgets, audit log, admin surface — an independent lean implementation of a
similar idea. Do not confuse with `NextGen-Guardians` (the Java/Spring hackathon repo). If we ever
want to cross-pollinate ideas (e.g. the Go proxy's budget/cache design), that's the place.

## Open questions / next steps

- [x] Notes folder (`notes/`) created 2026-10-27 evening session; deck studied (`01`), code mapped (`02`).
- [ ] Verify the uncommitted changes compile + tests pass before committing (past session context unclear).
- [ ] Decide whether to commit as one "fix gateway wiring + frontend live/sim fallback" commit or split.
- [ ] Deck screenshots: all placeholders (`[ APPLICATION SCREENSHOT REQUIRED ]`, slides 6–15). Capture from the running app.
- [ ] Fix deck-vs-code mismatches before jury: risk weights (deck 35/20/15/20/10 vs code 40/25/20/15), "8-stage" vs 9-stage, "statistical hallucination scoring" wording, unverifiable numbers (99.8%, <45ms, 99.99%).
- [ ] Real security holes found in 02 worth fixing or disclosing: streaming endpoint bypasses pipeline; unauthenticated JWT minting; hardcoded JWT secret; RBAC not enforced on approval endpoints.

## Key findings from the code study (2026-10-27)

Full detail in `02-CODE-MAP.md`. Headlines:
- Pipeline is 9 stages; deck says 8 in some slides, 9 in others.
- Risk weights in code are 40/25/20/15 (dataSensitivity/role/category/model) — the deck's 5-factor 35/20/15/20/10 matrix doesn't exist.
- Approval queue is REAL (audit-service + ApprovalsView) — the deck roadmap wrongly lists it as Phase 2 future work.
- Policy "hot-reload" is decorative: governance-service stores policies, gateway doesn't read them.
- Audit default persistence is H2 in-memory — data lost on restart outside docker. "Tamper-evident" is unsupported.
