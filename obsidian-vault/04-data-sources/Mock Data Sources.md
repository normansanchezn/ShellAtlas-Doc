---
title: "Mock Data Sources"
type: "data-source"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - mock-data
---

# Mock Data Sources

## Summary

The demo corpus lives in KMM seed data and in-memory repositories.

## Related Files

- `core/data/demo/DemoSeed.kt`
- `core/data/demo/DemoDocumentRepository.kt`
- `core/data/demo/DemoSourcesRepository.kt`

## Development Notes

- Recovered Swift mock terminology can be ported into `DemoSeed`.
- The repository tree no longer depends on Swift package JSON mocks.
