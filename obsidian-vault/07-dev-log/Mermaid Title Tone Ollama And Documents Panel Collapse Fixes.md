---
title: "Mermaid Title, Tone, Ollama And Documents Panel Collapse Fixes"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - assistant
  - documents
  - config
---

# Mermaid Title, Tone, Ollama And Documents Panel Collapse Fixes

## What changed

- **Mermaid diagrams**: removed the "Flow diagram" / "Sequence diagram" / "Timeline diagram" / "Mermaid source" label that was rendered above every diagram in `AssistantRichContent.kt` (`MermaidDiagramCard`). The `MermaidDiagram.title` field was removed entirely — diagrams now render without a header.
- **Less robotic assistant copy** (`GroundedAssistantEngine.kt`): reworked `answerQuestion`, `explainFlow` and `summarize` to drop the rigid `## Overview / ## Detailed explanation / ## Key details / ## Related documents / ## What to do next` header scaffolding on every answer. Responses now read like a normal reply — intro line, the relevant content, an optional "Key points:" / "You might also find this useful: ..." line, and a short closing line inviting follow-up. Rewrote the EN/ES/FR `Copy` templates with warmer, more conversational wording. Also fixed a latent bug where `summarize()` rendered a `## Key details` heading instead of the `"Key points:"` copy the test suite checks for.
- **Ollama connectivity** (`.env`): root cause was that `.env` had none of the `USE_OLLAMA` / `OLLAMA_*` keys, so `AppConfig.useOllama` defaulted to `false` and the assistant always ran in `GroundedAssistantEngine`-only mode, even with `ollama serve` running locally with models installed. Added:
  ```
  USE_OLLAMA=true
  OLLAMA_BASE_URL=http://127.0.0.1:11434
  OLLAMA_MODEL=llama3.2
  ```
  (the previous default model `llama3.1` isn't one of the installed models — `llama3.2`, `qwen3:8b`, `gemma4`, `qwen2.5-coder:7b` are). `OllamaClient`/`OllamaAssistantEngine`/`CompositeAssistantEngine` wiring itself was already correct — this was purely a missing env var.
- **Documents screen — Explorer/Attributes collapse**: previously collapsing either side panel was an instant width swap (230dp/240dp ↔ 22dp rail) with no transition, and the only way to collapse a panel in wide mode was indirectly (selecting a document auto-collapsed both). Now:
  - Added a `PanelCollapseButton` (chevron icon button) to `PanelControls.kt`.
  - `ExplorerTreePanel` header gets a "collapse" chevron-left button; `AttributesPanel` header gets a "collapse" chevron-right button — both call the existing `ToggleExplorerPanel` / `ToggleAttributesPanel` intents, so users can collapse either panel directly instead of only via auto-collapse-on-select.
  - Both panels' width now animates via `animateDpAsState` (`ShellMotion.durationMedium`, standard easing) between their resizable width and the 22dp `CollapsedPanelRail` width, with `Modifier.clipToBounds()` so content slides/clips smoothly instead of popping.

## Why

User feedback: mermaid diagrams shouldn't show a title, assistant answers feel too "robot"/templated, Ollama wasn't actually being used despite running locally, and the Documents explorer/attributes collapse looked broken (instant, jarring width changes with no way to collapse them manually).

## Supabase / Confluence audit (no code changes — infra/credentials needed)

Investigated why the app keeps showing demo data and the state of local/remote Supabase + Confluence:

- **Documents repository wiring is already correct** (`AppContainer.kt`): conditional on `config.api` → `config.supabase` → `DemoDocumentRepository`. The only reason it's on demo is that `.env` lacks `SUPABASE_ANON_KEY` (only `SUPABASE_SERVICE_ROLE_KEY` is present, which is a backend-only secret and intentionally not used client-side) and `API_BASE_URL` (the Hono backend on port 8787 isn't running).
- **Local Supabase**: `supabase status` fails — Docker daemon isn't running, so the local Supabase stack (which would provide a local anon key) can't be inspected/started.
- **Remote Supabase**: `backend/.env` has a real project URL (`xjnqzxbqtevuwuwqbunm.supabase.co`) and service-role key, but no anon key is available anywhere in the repo for the app to use.
- **Sources / Confluence**: `sourcesRepository` in `AppContainer.kt` is unconditionally `DemoSourcesRepository` — there is no `ApiSourcesRepository`/`SupabaseSourcesRepository`, and the backend (`backend/src/index.ts`) only implements `/v1/documents/*` + `/v1/assistant/intelligence` routes. `/v1/sync/confluence` (and Jira/Azure DevOps) are documented in `backend/README.md` as planned but not implemented. Confluence only exists as seeded demo data (`DemoSeed.kt`, source id `source-confluence`).

**This is why the user keeps seeing "the same mocks"**: it's not a bug in the demo/real switch — it's that no real backend is reachable and Confluence sync was never implemented server-side. Making this "perfect" requires, in order:
1. Start Docker + `supabase start` (local) to get a local anon key, **or** get the anon key for the remote project `xjnqzxbqtevuwuwqbunm.supabase.co`.
2. Add `SUPABASE_ANON_KEY` and `API_BASE_URL=http://127.0.0.1:8787` to `.env` and run the backend (`cd backend && npm run dev` or equivalent).
3. Implement `/v1/sync/confluence` in the backend (Atlassian OAuth — `ATLASSIAN_CLIENT_ID`/`ATLASSIAN_CLIENT_SECRET` are referenced in `backend/.env` but not set) plus an `ApiSourcesRepository`/`SupabaseSourcesRepository` and wire it into `AppContainer.sourcesRepository` conditionally, the same way `documentRepository` already is.

None of this was faked/stubbed — per the "don't invent things that don't exist" instruction, Sources/Confluence stays on `DemoSourcesRepository` until a real sync backend exists.

## Follow-ups

- Once `.env` has a real anon key + `API_BASE_URL`, verify `AppConfig.isDemoMode` flips to `false` and Documents/Dashboard pull from Supabase/the API.
- Confluence/Jira/Azure DevOps sync is a backend feature (OAuth + sync endpoints) — out of scope for a config fix, needs its own implementation pass.
