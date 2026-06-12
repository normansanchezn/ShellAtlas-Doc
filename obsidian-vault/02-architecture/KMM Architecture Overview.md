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

The root `App` composable now applies `WindowInsets.safeDrawing` before handing off to auth or workspace content, so Android devices with notches, iOS devices with home indicators, and other inset-sensitive surfaces keep critical content inside the safe area.

Platform-specific loader code stays isolated: Android, iOS, desktop and wasm all resolve config through their own source sets, while the shared app shell remains portable and does not depend on platform-only APIs.

The iOS host app is now wired correctly for simulator testing: its Xcode project points to the real `Configuration/Config.xcconfig` file and includes the expected Preview Content folder, so the simulator build can complete instead of stopping at project validation.

## Mermaid Diagram

- [[Architecture Diagram]]
- [[Clean Architecture Diagram]]
- [[Data Flow Diagram]]

## Open Questions

- Which future live integrations should land first after the demo phase?
- Should the release config contract move to a signed manifest after the MVP?
