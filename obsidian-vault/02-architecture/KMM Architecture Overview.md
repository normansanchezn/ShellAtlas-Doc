---
title: "KMM Architecture Overview"
type: "architecture"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - architecture
  - clean-architecture
---

# KMM Architecture Overview

## Summary

ShellDoc uses Clean Architecture and MVI in a Kotlin Multiplatform layout.

## Layers

- `core/domain`: entities, repositories, use cases.
- `core/data`: demo data, repositories, engines, API adapters.
- `feature/*/presentation`: intent/state/effect/view models.
- `feature/*/ui`: Compose UI.
- `composeApp` and `iosApp`: platform hosts.

## Data Flow

Domain rules own search, retrieval and health heuristics. Data repositories provide deterministic demo content today and can be replaced by remote adapters later.

Runtime configuration is now normalized through `AppEnvironment` and platform loaders so the same app can ship DEV and PROD bundles without duplicating business logic.

## Mermaid Diagram

- [[Architecture Diagram]]
- [[Clean Architecture Diagram]]
- [[Data Flow Diagram]]

## Open Questions

- Which future live integrations should land first after the demo phase?
- Should the release config contract move to a signed manifest after the MVP?
