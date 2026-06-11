---
title: "Assistant Natural Replies And Document Creation"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - assistant
---

# Assistant Natural Replies And Document Creation

## What changed

- Added `AssistantIntentType.CREATE_DOCUMENT` with bilingual (EN/ES) trigger phrases.
- New `CreateDocumentFromAssistantUseCase`: turns "create a document about X" / "crea un documento sobre X" into a real draft via `CreateDocumentUseCase`, respecting `Permission.EDIT_DOCUMENTS`.
- `AskAssistantUseCase` routes `CREATE_DOCUMENT` straight to creation, bypassing cache/grounding.
- `GroundedAssistantEngine.notEnoughInformation` no longer returns a flat "no information" error — it replies naturally in the user's detected language, suggests alternative search terms, and offers to create a draft.
- Wired `roleProvider` and `CreateDocumentFromAssistantUseCase` into `AppContainer.assistantViewModel()`.

## Why

Product review against [[KMM Product Definition]] and [[AI Rules]]: the assistant must always feel useful and conversational, never dead-end the user, and should be able to act (create documentation) from chat, not just answer.

## Follow-ups

- Consider letting the assistant suggest *which existing document* is closest when grounding is empty (fuzzy match on `KnowledgeQueryExpander` terms) before offering to create a new one.
- Consider a chat affordance to open a just-created draft directly in the editor (currently shown only as a cited source).
