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
