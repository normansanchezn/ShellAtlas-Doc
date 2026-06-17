---
title: "Assistant"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-17
tags:
  - shelldoc
  - assistant
  - ai-ready
---

# Assistant

## Summary

Deterministic grounded assistant for Q&A, flow explanation, improvement guidance and document creation. It now answers with longer structured sections, can render context-aware Mermaid diagrams inside the conversation, can still turn a chat request into a new draft document, and on mobile now keeps each reply in the detected language of the current user turn instead of mixing bilingual system copy into the thread.

## Related Files

- `core/domain/usecase/assistant`
- `core/data/assistant`
- `feature/assistant`

## Data Flow

Question -> intent detection -> grounding retrieval -> assistant engine -> cited answer.

`CREATE_DOCUMENT` intent skips grounding/cache and goes straight to `CreateDocumentFromAssistantUseCase`, which creates a draft via `CreateDocumentUseCase` and confirms it back to the user with a link-style source.

When the detected intent is a flow/process explanation, `GroundedAssistantEngine` now routes the answer through `AssistantMermaidBuilder` and `AssistantRichContent`, so the chat can show a readable diagram instead of plain fenced code.

## Intents

- `QUESTION`, `EXPLAIN_FLOW`, `IMPROVE_DOCUMENT`, `SUMMARIZE` — grounded answers from `GroundedAssistantEngine`.
- `CREATE_DOCUMENT` — trigger phrases in EN/ES/FR ("create a document about...", "crea un documento sobre...", "cree un document sur..."); generates a titled draft with a Summary/Details/Open Questions skeleton and creates it via `CreateDocumentUseCase`. Respects `Permission.EDIT_DOCUMENTS` and explains in natural language when the role can't create.
- Guided KT / onboarding requests ("onboarding", "knowledge transfer", "kt session", "nuevo colaborador", "nouveau collaborateur"...) are expanded by `KnowledgeQueryExpander` to `["onboarding", "authentication", "release process"]` and routed to `EXPLAIN_FLOW` via `DetectAssistantIntentUseCase` FLOW_MARKERS, so a new collaborator gets a step-by-step walkthrough grounded on the Authentication and Release Process docs.

## Welcome Message

- `BuildWelcomeMessageUseCase` returns a localized (EN/ES/FR) greeting shown by `AssistantViewModel` whenever `messages` is empty — on `Initialize` and on `StartNewConversation`.
- The greeting tells the user it can: open/show a document, point to where to find documentation on a topic, share documentation analytics, walk through a guided KT for new collaborators, or create a new draft document.
- No DI wiring needed: `BuildWelcomeMessageUseCase` has a no-arg constructor and `AssistantViewModel`'s `buildWelcomeMessage` param defaults to `BuildWelcomeMessageUseCase()`.

## Multilingual Replies (EN/ES/FR)

- `DetectAssistantLanguageUseCase` detects EN/ES/FR from diacritics and common function words (defaults to English; falls back to a caller-supplied default when no hints match).
- `GroundedAssistantEngine` and `CreateDocumentFromAssistantUseCase` each hold a localized `Copy` table (EN/ES/FR) for every template string — questions, flow walkthroughs, improvement advice, summaries, "not enough information", and document-creation confirmations.
- `OllamaAssistantEngine`'s prompt instructs the LLM to reply in the same language the user wrote in (EN/ES/FR), defaulting to English if unsure.
- `AssistantViewModel` now starts a new conversation in English by default, infers the active language from each user message, and replies only in that detected language. The previous bilingual "switching language" system message was removed so the mobile transcript stays clean.

## Source Card Navigation

- Source cards in `SourcesList`/`ChatMessageBubble` are clickable (`onSourceClick`). `AssistantScreen` wires this to `onOpenDocument(source.documentId)`, which `WorkspaceShell` binds to `AppNavigator::openDocument` — see [[Documents]] "Open From Assistant".

## Mermaid Diagram

- [[Assistant Mermaid And Persistence Flow]]

## Development Notes

- Alias expansion from the recovered Swift search logic is reused for grounding and search.
- When no documents match, the assistant replies in the user's detected language (EN/ES/FR) with alternative search terms and offers to create a draft instead of returning a flat "no information" message.
- `GroundedAssistantEngine` now answers with sections like overview, detailed explanation, key details, related documents and next steps, so the assistant no longer feels overly terse.
- `AssistantMermaidBuilder` chooses `flowchart`, `sequenceDiagram` or `gantt` heuristically from the question and top grounding document.
- Flow/process questions now explicitly prefer `flowchart`; `gantt` is reserved for requests that clearly ask for timeline/schedule/milestone style output.
- `AssistantRichContent` parses Mermaid fences and renders a responsive Compose representation for flow, sequence and timeline diagrams inside `ChatMessageBubble`.
