---
title: "AI Rules"
type: "ai-rules"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - ai-rules
---

# AI Rules

## Summary

ShellDoc uses deterministic, testable assistant behavior by default.

## Rules

- Ground every answer in retrieved documents.
- Prefer deterministic heuristics before model generation.
- Keep improvement advice tied to explicit document issues.
- Treat live LLM integrations as optional adapters, not core behavior.
- Never reply with a bare "no information" error. When nothing is indexed, respond naturally, in the user's language, suggest alternative terms and offer to create a draft document.
- Treat "create a document about X" as an action: generate a titled draft (Summary/Details/Open Questions) and create it via `CreateDocumentUseCase`, respecting `Permission.EDIT_DOCUMENTS`.
- Detect the user's language (English, Spanish or French) via `DetectAssistantLanguageUseCase` and reply in that same language; default to Spanish for the welcome message and English when no language hints are found in a question.
- On opening the assistant with an empty conversation, send a localized welcome message (`BuildWelcomeMessageUseCase`) offering to: show a document, point to documentation on a topic, share documentation analytics, run a guided KT for new collaborators, or create a new draft document.
