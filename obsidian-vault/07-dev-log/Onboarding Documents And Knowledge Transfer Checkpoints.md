---
title: "Onboarding Documents And Knowledge Transfer Checkpoints"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - assistant
  - dashboard
  - onboarding
---

# Onboarding Documents And Knowledge Transfer Checkpoints

## What changed

- New `DemoSeed` documents under `platform = "Onboarding"`: **"Llega a Shell"** (team structure, where docs live, how the assistant/KT works) and **"Android Developer Setup"** (mock repo clone, `local.properties`, env vars / secrets — all clearly marked `MOCK`/`TODO`). `DocumentTreeBuilder` groups by platform, so both appear automatically under a new "Onboarding" folder in Documents.
- New domain layer for guided Knowledge Transfer: `KnowledgeCheckpoint` and `KnowledgeProgress` entities, `KnowledgeCheckpointRepository` interface, and use cases `GetKnowledgeCheckpointsUseCase`, `GetKnowledgeProgressUseCase`, `CompleteKnowledgeCheckpointUseCase`, `BuildKnowledgeTransferMessageUseCase` (EN/ES/FR step + completion copy), `DetectCheckpointCompletionUseCase` (keyword-based "listo/done/terminé" detector).
- `DemoKnowledgeCheckpointRepository` (in `core/data/demo`): fixed ordered checklist of 5 steps — the 2 new onboarding docs plus Authentication, Loyalty Rewards Flow and Release Process — tracked in-memory for the session.
- `AssistantViewModel` gained `StartKnowledgeTransfer` intent: posts the next checkpoint's instruction ("revisa X, luego dime 'listo'"), and `send()` now checks, while a checkpoint is active, whether the user's message is a "done" acknowledgement before falling back to the normal grounded answer flow — if so it marks the checkpoint complete and posts the next step (or a completion summary with the knowledge score).
- `AssistantHeader` gained a "Knowledge Transfer" entry button (shows step counter / completion %) and an animated progress bar while a checkpoint is active.
- `DashboardMetrics` gained `knowledgeCheckpointsCompleted`, `knowledgeCheckpointsTotal`, `projectKnowledgeScorePercent`; `DerivedDashboardRepository` now also depends on `KnowledgeCheckpointRepository` to compute them. `DashboardScreen` adds a 5th "Project Knowledge" metric card (wraps into a second row in wide layout).
- `AppContainer` wires a single shared `DemoKnowledgeCheckpointRepository` instance into both the dashboard repository and the assistant view model, so Dashboard and Assistant reflect the same progress.

## Why

Per the user's request: a new user opening the app should get an easy, guided Knowledge Transfer with step-by-step tracked progress ("revisa X, luego dime cuando termines"), backed by real onboarding documents (team setup, computer setup with mock secrets), and a "how much do you know about the project" metric on the Dashboard. This makes the "guided KT" promise already present in [[Assistant Welcome Message And Multilingual Replies]]'s welcome copy actually functional.

## Follow-ups

- Checkpoint completion state is in-memory per session (resets on app restart), matching the rest of the demo data layer.
- Checkpoint progression assumes the user follows the KT flow in order (next checkpoint = `checkpoints[progress.completed]`); if a real backend is added, track completed IDs explicitly rather than by count.
- "Done" detection is a simple keyword match across EN/ES/FR; consider routing through the LLM if false positives/negatives become an issue.
