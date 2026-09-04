# Architecture and Decisions

## System context

```mermaid
flowchart LR
  Human[Human reviewer] --> UI[Interview dashboard]
  UI --> API[Spring REST API]
  API --> Links[URL shortener domain]
  API --> Orch[Governed orchestrator]
  Links --> H2[(H2 via JPA)]
  Orch --> Graph[Dependency graph]
  Orch --> Primary[LangChain4j agent]
  Primary --> RAG[Knowledge retrieval]
  Primary -. connected mode .-> LLM[OpenAI-compatible LLM]
  Orch --> Fallback[Safe fallback agent]
  Orch --> Policy[Guardrail policy]
  Orch --> H2
  API --> Audit[Audit and metrics]
  Audit --> H2
```

## Workflow

```mermaid
flowchart TD
  R[Requirements] --> D[Architecture]
  D --> DEV[Development]
  D --> SEC[Security review]
  D --> DOC[Documentation]
  DEV --> QA[QA]
  QA --> SYNC{Synchronization}
  SEC --> SYNC
  DOC --> SYNC
  SYNC --> H{Human approval}
  H --> REL[Release readiness]
  DEV -. upstream change .-> RP[Replan and invalidate]
  RP --> R
  QA -. terminal failure .-> SS[Safe stop]
  H -. risk rejected .-> RB[Rollback]
```

## Defensible choices

- **Modular monolith:** easy to evaluate, with domain, AI, orchestration, policy, persistence, HTTP, and UI boundaries that can be extracted later.
- **Java 21 target:** virtual threads support concurrent ready-node execution with a conventional enterprise LTS baseline.
- **Spring Boot:** recognizable enterprise DI, HTTP, validation, actuator, JPA, and configuration conventions.
- **Typed outputs:** agents return summary, artifacts, evidence, risks, retrieved context, and mode instead of unstructured strings.
- **Provider-independent governance:** models do not decide whether they are allowed to run. The orchestrator and policy layer own dependency, approval, retry, fallback, and terminal-state rules.
- **H2 default:** zero-setup interview reliability with a direct migration path through Spring Data JPA.
- **Optional LLM:** a missing/failed credential never blocks the core demo; the UI discloses the active execution mode.
- **Local RAG:** deterministic evidence grounding without secrets. It is intentionally not described as vector search.
- **Scenario and scope policy:** requirements outside the URL/link-service domain and clear Greenfield/Brownfield mismatches are rejected before a workflow is persisted.
- **Evidence catalog:** stage outputs reference stable artifact IDs. A read-only allowlist maps those IDs to real repository files and generated Maven/JaCoCo reports without exposing arbitrary filesystem access.
- **Cross-stage context:** each agent prompt includes the validated summaries from its declared dependency nodes; downstream execution therefore carries forward reviewed upstream decisions.
- **Accountable decisions:** approve and rollback operations require both an identified reviewer and a non-empty reason. This is a prototype boundary, not a substitute for authenticated RBAC.
- **Final summary and MTTR:** successful runs consolidate plan, rationale, artifacts, risks, validation, assumptions, and limitations, while reliability metrics expose mean time to recovery from recorded failure/recovery timestamps.

## Extension seams

- Replace `UrlPolicy` for DNS/private-network and enterprise reputation checks.
- Replace Spring Data repositories/configuration with PostgreSQL.
- Replace `KnowledgeService` with LangChain4j embeddings and a vector store.
- Add MCP tools behind an `AgentExecutor` implementation and enforce tool allowlists in `GuardrailPolicy`.
- Add new workflow topologies through `WorkflowGraphProvider` without changing the engine.
- Export audit and reliability telemetry through OpenTelemetry/Micrometer.

## Production limitations

The prototype still needs authenticated RBAC, approval authorization, DNS resolution and rebinding defenses for hostname targets, encrypted/tamper-evident audit storage, distributed workflow locking, idempotency keys, quotas and abuse controls, secrets management, model evaluation datasets, and production vector infrastructure. Direct loopback, private, link-local, multicast, and cloud metadata IP destinations are rejected.
