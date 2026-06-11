---
title: "Updates"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - updates
  - health
---

# Updates

## Summary

Triage view over documents that need review or improvement.

## Related Files

- `core/domain/usecase/assistant/EvaluateDocumentHealthUseCase.kt`
- `core/data/repository/DerivedPendingUpdatesRepository.kt`
- `feature/updates`

## Development Notes

- Health remains deterministic.
- Pending updates are derived from shared domain heuristics.
