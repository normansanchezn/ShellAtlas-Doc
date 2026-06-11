---
title: "Assistant"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - assistant
  - ai-ready
---

# Assistant

## Summary

Deterministic grounded assistant for Q&A, flow explanation and improvement guidance.

## Related Files

- `core/domain/usecase/assistant`
- `core/data/assistant`
- `feature/assistant`

## Data Flow

Question -> intent detection -> grounding retrieval -> assistant engine -> cited answer.

## Mermaid Diagram

- [[KMM Recovery Flow]]

## Development Notes

- Alias expansion from the recovered Swift search logic is reused for grounding and search.
