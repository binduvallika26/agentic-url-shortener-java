# ForgeFlow AI Interview Demo Guide

Use this guide to demonstrate the application from start to finish. The recommended presentation takes approximately 12-15 minutes.

## 1. Before the interview

Open PowerShell and run the automated tests:

```powershell
cd C:\Users\bindu\Assignment-Agentic\agentic-url-shortener-java
.\scripts\test.ps1
```

Confirm that the output contains:

```text
Tests run: 9, Failures: 0, Errors: 0
BUILD SUCCESS
```

Start the application:

```powershell
.\scripts\run.ps1
```

Keep that PowerShell window open. Open these pages in separate browser tabs:

- Dashboard: http://localhost:8080/
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Test coverage: `target/site/jacoco/index.html`

## 2. Opening explanation

Say:

> This is ForgeFlow AI, a governed agentic software-engineering system. The URL shortener is the working product used to demonstrate how specialized agents can transform a stakeholder requirement into a reviewable engineering outcome. The orchestration engine controls dependencies, approvals, retries, fallback, rollback, replanning, auditability, and release readiness.

Explain who supplied the requirement:

> The assignment brief represents the initial business-stakeholder requirement. In production, this input could come from a product owner, approved ticket, requirements document, or connected work-management system. I act as the accountable engineer and human reviewer.

Point out the capability indicator in the header and say:

> The default DEMO mode uses deterministic agent responses so the interview does not depend on an API key or network connection. LangChain4j is integrated, and a real model can be enabled separately. The model is a replaceable worker; governance remains deterministic application logic.

## 3. Greenfield workflow

Select **Greenfield**. Use this engineering requirement:

```text
Build a secure URL shortener with custom aliases, expiration, visit analytics, audit logging, and safe release controls.
```

Do not enter a destination URL in this field. It expects a description of software that should be built or changed.

Click **Launch agent workflow**.

Say:

> This creates a stateful workflow run and records its scenario, requirement, dependency graph, revision, actor, and decision history.

Click **Execute ready agents**.

Explain the graph:

1. Requirements executes first.
2. Architecture depends on Requirements.
3. Development, Security Review, and Documentation branch after Architecture.
4. QA depends on Development.
5. Release Readiness synchronizes QA, Security Review, and Documentation.
6. Release Readiness stops for human approval.

Say:

> This is an explicit dependency graph, not a simple prompt chain. Independent stages can run in parallel, and Release cannot proceed until all required branches synchronize.

Confirm that the run displays `AWAITING_APPROVAL`. Click **Approve waiting gate**.

Say:

> Release is high impact, so the system cannot approve its own release. An identified human must review the evidence and approve the gate.

Click **Execute ready agents** again. Confirm that the run becomes `SUCCEEDED`.

## 4. Agent evidence

Scroll to **Agent Evidence**.

Show that each completed step contains:

- A typed result and concise summary
- Generated artifact identifiers
- Validation evidence
- Risks
- Retrieved grounding context
- Execution mode

Say:

> Agent output is treated as evidence requiring validation, not as automatically trusted text. Typed results and recorded risks make each stage reviewable and auditable.

## 5. Brownfield workflow

Select **Brownfield**. Use:

```text
Enhance the existing URL shortener with expiring links and visit analytics without breaking current redirects.
```

Launch and execute the workflow.

Say:

> Brownfield work changes an existing system. The agents must preserve API compatibility, identify impacted modules and data flows, consider regression risk, and validate existing redirect behavior.

Complete the human approval gate and finish the run.

## 6. Ambiguous requirement

Select **Ambiguous**. Use:

```text
Make links smart.
```

Click **Launch agent workflow**, then **Execute ready agents**.

Confirm that Requirements becomes `AWAITING_APPROVAL` before agent execution continues.

Say:

> The phrase “smart” is not sufficiently defined. It might mean analytics, personalized redirects, automatic expiration, or user tracking. The system does not invent product or privacy scope. It stops for stakeholder clarification, demonstrating controlled autonomy.

For the short interview path, leave this scenario paused after explaining the gate.

## 7. Retry and fallback

Click **Demo development fallback**. It selects Brownfield and inserts a controlled failure marker.

Click **Launch agent workflow**, then **Execute ready agents**.

In Agent Evidence, locate Development and show:

```text
attempts: 2
fallbackUsed: true
```

Say:

> The primary Development agent is intentionally unavailable. The orchestrator performs two bounded attempts and then invokes a conservative review-only fallback. Other agents remain unaffected. This prevents infinite retries and uncontrolled execution.

A fresh fallback demonstration adds exactly two primary attempts and one fallback step to the cumulative metrics.

## 8. Dynamic replanning

Launch any normal workflow. Edit the engineering requirement to add a new condition, for example:

```text
Build a secure URL shortener with aliases, expiration, analytics, audit logging, and a requirement that expired links return HTTP 410.
```

Click **Replan edited requirement**.

Say:

> An upstream requirement change invalidates downstream assumptions. Replanning increments the revision, clears prior approvals, invalidates earlier step results, and returns the workflow to a governed pending state.

## 9. Rollback

After at least one workflow stage succeeds, click **Rollback**.

Confirm that the run displays `ROLLED_BACK` and check the Audit Timeline.

Say:

> Rollback records the human decision, marks completed work as rolled back, stops further execution, and preserves the decision lineage. In production, each stage would also connect to a technical compensation or deployment rollback action.

## 10. Working URL-shortener product

Scroll to **URL Product Demo**. Enter:

```text
https://example.com
```

Use a unique alias such as:

```text
demo42
```

Click **Create**. Open the generated link:

```text
http://localhost:8080/demo42
```

Verify that:

- The browser redirects to `https://example.com`.
- The Audit Timeline records `link.create · success`.
- Opening the short link records `link.resolve · success`.
- The visit count increases.

Say:

> The product is not a visual mock. It has a real Spring Boot API, HTTP redirects, custom aliases, expiration support, visit analytics, URL policy enforcement, audit logging, and persistent H2 storage.

Explain the local address if asked:

> The assignment requests a runnable prototype rather than public deployment. Therefore, localhost is the expected demonstration address.

## 11. Reliability and audit evidence

Show **Reliability** and explain:

- **Total runs:** All persisted workflow executions
- **Successful completion:** Percentage of runs that reached `SUCCEEDED`
- **Retry attempts:** Attempts beyond the first execution attempt
- **Fallback steps:** Individual stages completed using fallback
- **Runs rolled back:** Percentage of runs explicitly rolled back
- **Mean end-to-end latency:** Average duration of completed runs

Say:

> These metrics are cumulative because H2 preserves previous runs. They show operational behavior across the stored workflow history, not only the currently selected run.

Show **Audit Timeline** and say:

> Every important action records the actor, action, outcome, timestamp, resource, and correlation identifier. This provides traceability across user, workflow, and product operations.

## 12. Swagger, health, and tests

Open Swagger UI and show the link, workflow, approval, replan, rollback, audit, metrics, knowledge, and capability endpoints.

Open the health endpoint and confirm that the application is `UP`.

Show the successful test output and JaCoCo report. Say:

> The nine automated tests cover the real Spring, HTTP, and JPA stack; secure URL validation; graph topology; parallel-path synchronization; release approval; ambiguous-requirement gating; local retrieval grounding; bounded retries; and explicit fallback behavior.

## 13. Limitations and production path

Be explicit about the prototype boundaries:

> This is a production-minded prototype, not a completed production deployment. A production version would add authentication and role-based access control, PostgreSQL, rate limiting, DNS resolution with private-network protection, distributed workflow coordination, tamper-resistant audit storage, OpenTelemetry, and deployment compensation actions.

Also state:

- Local lexical retrieval is implemented; a vector database is not claimed.
- LangChain4j integration is implemented; the real external model is optional and disabled by default.
- MCP is an extension point and is not currently connected.
- The fallback produces a conservative review artifact rather than silently pretending that normal execution succeeded.

## 14. Closing statement

End with:

> The key design principle is that agents execute within defined autonomy boundaries, while humans retain oversight, approvals, and final quality ownership. The URL shortener proves the product path works, and the orchestration layer demonstrates safe, observable, and defensible lifecycle automation.

## Recommended 12-minute timing

| Time | Demonstration |
|---|---|
| 0:00-1:00 | Purpose, stakeholder requirement, and capability mode |
| 1:00-4:00 | Greenfield graph, parallel branches, approval, and completion |
| 4:00-5:00 | Agent evidence and grounding |
| 5:00-6:00 | Brownfield explanation |
| 6:00-7:00 | Ambiguous requirement and human clarification gate |
| 7:00-8:00 | Retry and Development fallback |
| 8:00-9:00 | Replan and rollback |
| 9:00-10:00 | Working URL shortener and redirect |
| 10:00-11:00 | Reliability metrics and audit timeline |
| 11:00-12:00 | Swagger, tests, limitations, and closing |

## Quick recovery notes

- If port 8080 is unavailable, stop the previous Java process with `Ctrl+C` and rerun `scripts\run.ps1`.
- If an alias already exists, choose another alias such as `demo43`.
- If the UI shows old values, restart the application and hard-refresh the browser.
- High metric totals are not necessarily errors; the metrics include previous H2-persisted runs.
- Stop the application after the demonstration by pressing `Ctrl+C` in its PowerShell window.
