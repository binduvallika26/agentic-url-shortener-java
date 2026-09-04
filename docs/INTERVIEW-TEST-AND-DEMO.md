# ForgeFlow AI Interview Test and Demo Guide

## Purpose of the application

ForgeFlow AI is a working URL shortener combined with a governed agentic software-development workflow. The URL product creates and resolves short links. The workflow demonstrates how specialized agents process an engineering requirement through Requirements, Architecture, Development, Security, QA, Documentation, and Release Readiness while the application enforces dependencies, evidence, human approvals, retries, fallback, rollback, safe stop, audit history, and reliability metrics.

Use this sentence to open the interview:

> This application demonstrates controlled agent autonomy across the software-development lifecycle. Agents execute only eligible tasks, evidence moves between dependent stages, and an accountable human owns clarification, release approval, and rollback decisions.

## 1 Before the interview

Open PowerShell:

```powershell
cd C:\Users\bindu\Assignment-Agentic\agentic-url-shortener-java
.\scripts\test.ps1
```

Expected result:

```text
Tests run: 28, Failures: 0, Errors: 0
All coverage checks have been met.
BUILD SUCCESS
```

The build enforces at least 96 percent instruction coverage. Open the detailed report if requested:

```powershell
start .\target\site\jacoco\index.html
```

Start the application:

```powershell
.\scripts\run.ps1
```

Wait for a message similar to:

```text
Started AgenticUrlShortenerApplication
```

Open these tabs:

- Dashboard: http://localhost:8080
- Swagger API: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- H2 console: http://localhost:8080/h2-console

If port 8080 is already in use:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
Stop-Process -Id <OwningProcess>
.\scripts\run.ps1
```

After restarting, press `Ctrl+F5` in the browser.

## 2 Explain the screen

1. **Requirement Studio** accepts a software engineering requirement, not a destination URL.
2. **Live Run** displays the dependency graph and current state of every agent.
3. **Agent Evidence** displays stage-specific summaries, propagated upstream context, validation, risks, and links to real repository artifacts.
4. **Reliability** displays cumulative persisted workflow metrics.
5. **URL Product Demo** creates an actual short URL and tracks visits.
6. **Audit Timeline** records the actor, action, outcome, time, and correlation identifier.

The accountable reviewer field should contain your name:

```text
Bindu Vallika
```

## 3 Greenfield scenario

Greenfield means building a new system or feature.

Select **Greenfield** and enter:

```text
Build a secure URL shortener with custom aliases, expiration, visit analytics, audit logging, and safe release controls.
```

Click **Launch agent workflow**.

Expected output:

- A new workflow identifier and revision 1 appear.
- Requirements is ready; dependent stages are pending.
- The Audit Timeline records `workflow.start` with your name.

Click **Execute ready agents**.

Expected output:

- Requirements succeeds first.
- Architecture succeeds after Requirements.
- Development, Security Review, and Documentation execute after Architecture.
- QA executes after Development.
- Release Readiness waits until QA, Security, and Documentation synchronize.
- The run stops at `AWAITING_APPROVAL`.

Explain:

> This is an explicit dependency graph rather than a simple prompt chain. Independent stages can execute in parallel, but Release cannot proceed until all required branches synchronize.

Review **Agent Evidence**. Point out:

- Requirement-specific acceptance criteria
- `upstream-context`, proving dependent agents received validated upstream summaries
- Grounding evidence
- Risks and trade-offs
- Clickable source, test, architecture, and coverage artifacts

Click **Approve release gate** and give the reason:

```text
Architecture, security, QA, and documentation evidence reviewed.
```

Click **Execute ready agents** again.

Expected output:

- Run status becomes `SUCCEEDED`.
- Release Readiness becomes `SUCCEEDED`.
- A **Final Engineering Summary** appears with plan, rationale, artifacts, risks, validation, assumptions, and limitations.
- Audit Timeline records approval and stage decisions.

## 4 Brownfield scenario

Brownfield means enhancing or fixing an existing service while protecting current behavior.

Select **Brownfield** and enter:

```text
Enhance the existing URL shortener with link expiration and visit analytics without breaking current redirects or API behavior.
```

Launch and execute the workflow.

Expected output:

- Requirements evidence includes preservation of existing redirects and API compatibility.
- Architecture identifies impacted API, service, persistence, policy, and audit components.
- Development is treated as high impact.
- The same dependency, validation, and release approval controls apply.

Explain:

> Brownfield work emphasizes impact analysis, backward compatibility, regression testing, and safe change management. Greenfield focuses on defining and building a new capability.

Approve and execute again to complete the run.

## 5 Ambiguous requirement

Select **Ambiguous** and enter:

```text
Make links smart.
```

Click **Launch agent workflow**, then **Execute ready agents**.

Expected output:

- Status becomes `AWAITING_APPROVAL` at Requirements.
- No downstream agent runs.

Explain:

> Smart is undefined. It could mean analytics, personalized redirects, automatic expiration, or tracking. The system stops instead of inventing product or privacy scope.

Replace the requirement with:

```text
Add user-selected expiration to new short links and return HTTP 410 when an expired link is opened.
```

Click **Replan edited requirement**.

Expected output:

- The workflow identifier stays the same.
- Revision increases.
- Previous stage results and approvals are invalidated.
- Replans increases by one; Total runs does not increase.

Click **Execute ready agents**, approve the clarified Requirements gate, and execute again. Continue through the Release approval gate.

## 6 Scope and scenario validation

Select Brownfield and enter:

```text
Build a snake game and open it on a new page.
```

Expected output:

```text
out_of_scope
```

Select Brownfield and enter:

```text
Build a new URL shortener from scratch.
```

Expected output:

```text
scenario_mismatch
```

Explain:

> Scope and scenario policy prevents the system from producing convincing but irrelevant evidence for work outside the URL-shortener assignment.

## 7 Retry and fallback

Click **Demo development fallback**, then launch and execute.

Expected Development evidence:

```text
attempts: 2
fallbackUsed: true
mode: FALLBACK
```

Expected Reliability changes:

- Retry attempts increases.
- Fallback steps increases.
- Mean time to recovery receives a recovery sample.

Explain:

> The Development provider is intentionally failed. The orchestrator performs two bounded attempts and then uses a conservative review-only fallback, preventing infinite retries.

## 8 Rollback

Launch a normal workflow and execute until stages have succeeded. Click **Rollback** and provide:

```text
Risk rejected during accountable review.
```

Expected output:

- Run becomes `ROLLED_BACK`.
- Completed stage states become rolled back.
- Rollback frequency changes.
- Audit Timeline records `workflow.rollback` and your reason.

Explain that this prototype compensates workflow state. A production deployment adapter would perform the corresponding technical rollback.

## 9 URL shortener product

In **URL Product Demo**, enter:

```text
Destination URL: https://example.com/engineering
Custom alias: demo1
```

Click **Create**.

Expected output:

```text
Created http://localhost:8080/demo1 · visits 0
```

Click the generated short link.

Expected output:

- A new tab redirects to `https://example.com/engineering`.
- The dashboard visit count changes to 1 after refresh/polling.
- Audit Timeline records `link.create` and `link.resolve`.

Additional negative tests:

| Input | Expected result |
|---|---|
| `file:///secret` | `invalid_url` |
| `http://localhost/admin` | `invalid_url` |
| `http://10.0.0.1/admin` | `invalid_url` |
| Alias `x!` | `invalid_code` |
| Reuse alias `demo1` | `code_conflict` |
| Expiration in the past through Swagger | `invalid_expiry` |
| Open an expired stored link | HTTP 410 `expired` |

If the alias is empty, the service automatically generates an eight-character short code.

## 10 Reliability metrics

Explain each metric:

| Metric | Meaning |
|---|---|
| Total runs | Persisted workflow instances |
| Successful completion | Percentage ending in `SUCCEEDED` |
| Replans | Existing workflows revised after requirement changes |
| Retry attempts | Attempts after the first primary attempt |
| Fallback steps | Stages completed by the safe fallback |
| Runs rolled back | Percentage explicitly rolled back |
| Mean time to recovery | Average duration from a recorded failure to fallback or later successful recovery |
| Mean end-to-end latency | Average duration from workflow start to a terminal state |

Metrics are cumulative because the local H2 database persists previous runs. They do not reset when the browser refreshes.

## 11 Honest limitations

Say:

> This is a self-contained interview prototype. It demonstrates the governance model with real persistence, APIs, tests, evidence, and control flow. Production deployment would additionally require authenticated SSO and RBAC, tamper-evident centralized audit storage, distributed locking, rate limiting, complete DNS-rebinding defenses, secrets management, and deployment-specific compensation actions.

Do not claim that the deterministic demo agents deploy software or autonomously edit the repository. Connected LLM mode is optional and requires an explicitly configured API key.

## 12 Closing statement

> The important result is not an AI-generated paragraph. It is a stateful and reviewable engineering process: explicit dependencies, propagated evidence, validation gates, accountable approvals, bounded recovery, persistent audit history, and measurable reliability surrounding a working URL-shortener product.
