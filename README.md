# ForgeFlow AI - Agentic URL Shortener

Java 21 / Spring Boot prototype demonstrating a URL-shortener product plane and a governed agentic SDLC control plane. It includes H2 persistence, LangChain4j model integration, local retrieval grounding, typed agent results, an explicit dependency graph, parallel execution and synchronization, human approvals, bounded retries, fallback, safe stop, rollback, replan, audit, metrics, JUnit integration tests, JaCoCo coverage, Swagger, and an interview dashboard.

## Run

```powershell
cd C:\Users\bindu\Assignment-Agentic\agentic-url-shortener-java
.\scripts\run.ps1
```

Open:

- Dashboard: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console
- Health: http://localhost:8080/actuator/health
- JaCoCo after tests: `target/site/jacoco/index.html`

No database installation or AI key is required. H2 persists locally under `data/`. In default `DEMO` mode, agents are deterministic while still retrieving and attaching grounding evidence.

## Enable a real LLM

```powershell
$env:OPENAI_API_KEY="your-key"
$env:OPENAI_MODEL="gpt-4o-mini"
$env:AI_ENABLED="true"
.\scripts\run.ps1
```

The LangChain4j adapter then invokes the configured model. Do not commit keys. The dashboard reports `CONNECTED` only when both `AI_ENABLED=true` and a non-empty key are present.

## Test

```powershell
.\scripts\test.ps1
```

Current result: 28 tests, 0 failures; 96.2% instruction coverage and 78.5% branch coverage. Maven enforces a 96% instruction-coverage quality gate during `verify`. Tests cover the real Spring/JPA/HTTP stack, redirect visit counting, generated and custom codes, conflicts, missing and expired links, private-network URL policy, RAG retrieval, graph topology, ambiguity gates, scenario/scope validation, artifact availability, accountable approvals, final summaries, replanning and recovery metrics, synchronized release, rollback, bounded retry/fallback, and safe-stop behavior.

## Demo sequence

1. Open the dashboard and show the capability badge (`DEMO` or `CONNECTED`).
2. Launch Greenfield, click **Execute ready agents**, and show the release approval stop.
3. Inspect grounded agent evidence, propagated upstream context, risks, and links to real repository artifacts and test reports.
4. Click **Approve waiting gate**, execute again, and show the consolidated final engineering summary.
5. Click **Demo fallback**, launch, and execute to show two primary attempts plus fallback evidence.
6. Create a short link and open it; show audit and metrics updates.
7. Open Swagger and JaCoCo as API/test evidence.

## Honest capability statement

- LangChain4j: integrated and used when an API key is explicitly enabled.
- LLM: optional; disabled by default so the interview never depends on credentials.
- RAG: implemented as local lexical retrieval over an indexed engineering corpus; retrieved chunks are inserted into prompts and recorded in agent results.
- Vector database: not claimed. `KnowledgeService` is the replacement seam for a LangChain4j embedding store or enterprise vector database.
- MCP: not currently connected; an MCP tool adapter is a documented extension rather than a false claim.
- Agent artifacts: evidence links open a read-only, allowlisted view of real source, documentation, test, and coverage files. The demo does not claim that deterministic agents generated deployable code during the workflow.

See [architecture](docs/ARCHITECTURE.md), the [short demo guide](docs/DEMO.md), and the [complete interview demo script](docs/INTERVIEW-DEMO-GUIDE.md).
