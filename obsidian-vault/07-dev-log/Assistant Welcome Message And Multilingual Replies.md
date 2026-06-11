---
title: "Assistant Welcome Message And Multilingual Replies"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - assistant
---

# Assistant Welcome Message And Multilingual Replies

## What changed

- New `AssistantLanguage` entity (`ENGLISH`, `SPANISH`, `FRENCH`) and `DetectAssistantLanguageUseCase`: lightweight EN/ES/FR detector based on diacritics and common function words.
- New `BuildWelcomeMessageUseCase`: returns a localized (EN/ES/FR, default Spanish) greeting that lists what the assistant can do — show a document, point to documentation on a topic, share documentation analytics, run a guided KT for new collaborators, or create a draft document.
- `AssistantViewModel` now injects the welcome message as `messages.first()` whenever the conversation is empty — on `Initialize` and on `StartNewConversation`. No `AppContainer` wiring needed: `BuildWelcomeMessageUseCase` and the `buildWelcomeMessage` constructor param both have defaults.
- `GroundedAssistantEngine` and `CreateDocumentFromAssistantUseCase` now hold full localized `Copy` tables (EN/ES/FR) for every template string (questions, flow walkthroughs, improvement advice, summaries, "not enough information", document-creation confirmations), keyed off `DetectAssistantLanguageUseCase`. English copy was kept verbatim to the pre-refactor strings so existing tests stay green.
- `OllamaAssistantEngine`'s prompt now explicitly instructs the LLM to reply in the same language (EN/ES/FR) the user wrote in.
- `KnowledgeQueryExpander` gained onboarding/KT aliases ("onboarding", "knowledge transfer", "kt session", "nuevo colaborador", "nouveau collaborateur", ...) mapping to `["onboarding", "authentication", "release process"]`, and `DetectAssistantIntentUseCase` gained matching FLOW_MARKERS so guided-KT requests route to `EXPLAIN_FLOW` and ground on the Authentication / Release Process docs.
- Added tests: `DetectAssistantLanguageUseCaseTest`, `BuildWelcomeMessageUseCaseTest`, ES/FR cases in `GroundedAssistantEngineTest`, and `AssistantViewModelTest.initializeShowsWelcomeMessageWhenNoMessagesYet`. Fixed `newConversationClearsThread` (now expects the welcome message instead of an empty list).

## Why

Per [[Assistant]] and [[AI Rules]]: opening the assistant should never drop the user on an empty chat — it should proactively offer the things it can do (show docs, point to docs, analytics, guided KT, create a doc), and reply in whichever of EN/ES/FR the user actually used.

## Follow-ups

- No dedicated onboarding doc exists yet in `DemoSeed` — guided KT currently grounds on Authentication + Release Process as a starting walkthrough. Consider a real "New Collaborator Onboarding" doc.
- Language detection is heuristic (diacritics + function words); consider replacing with a small classifier if EN/ES/FR mixing in a single message becomes common.
