# Interview Demo Guide

## Opening

"This system transforms a requirement into reviewable engineering evidence. The model is a replaceable worker; governance stays deterministic. Humans own ambiguity resolution and release approval."

## Greenfield path

Launch the default requirement and execute. Point out that architecture unlocks development, security, and documentation; those branches synchronize with QA before release. The workflow stops at `AWAITING_APPROVAL`. Inspect evidence and approve release as the human reviewer.

## Ambiguous path

Select Ambiguous and use "Make links smart." Execute. Requirements remain `AWAITING_APPROVAL`, proving that the system does not invent product/privacy scope.

## Failure and fallback path

Click **Demo fallback**, launch, and execute. The primary executor deliberately fails twice; the fallback creates a conservative review-only artifact. Show `fallbackUsed`, attempts, decision lineage, and the reliability counter.

## Product path

Create alias `forge1` for a public URL, open the generated short link, then show the visit count and audit timeline. H2 means restarting the process does not lose the record.

## Evidence path

Open Swagger for API contracts and `target/site/jacoco/index.html` for coverage. Finish with the architecture diagrams and Git history.

## Do not overclaim

In default mode, LangChain4j is present but the external model is intentionally disabled. Local RAG grounding is real; vector search and MCP are future adapters. Say this clearly—transparent capability reporting is itself an enterprise AI governance feature.
