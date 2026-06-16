---
title: "Android Demo Tests"
type: "feature"
status: "active"
platform: "Android"
area: "ShellAtlas"
owner: "Product Engineering"
created: 2026-06-16
updated: 2026-06-16
tags:
  - shellatlas
  - android
  - demo
  - testing
---

# Android Demo Tests

## Summary

Instrumented Android walkthroughs that automate demo-ready flows for ShellAtlas and make screen recording repeatable.

## Purpose

Provide stable product tours for login, assistant, dashboard, documents, updates, sources and settings.

## User Problem

Manual demos are inconsistent and slow to reproduce when recording stakeholder videos.

## Expected Behavior

- Demo tests sign in using demo mode.
- Tests navigate the main app surfaces in a repeatable order.
- Optional pauses make it easier to capture clean screen recordings.

## Related Files

- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt`
- `composeApp/build.gradle.kts`
- `core/common/src/commonMain/kotlin/com/shelldocs/core/common/testing/DemoTestTags.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/SignInScreen.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatInputBar.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt`

## Domain Models

- No new domain models.

## UseCases

- Existing auth, assistant, dashboard, documents, updates, sources and settings use cases are exercised through UI flows.

## Repositories

- Demo repositories are used implicitly through app startup in `DEV` demo mode.

## ViewModels

- `AuthViewModel`
- `AssistantViewModel`
- `DashboardViewModel`
- `DocumentsViewModel`
- `UpdatesViewModel`
- `SourcesViewModel`
- `SettingsViewModel`

## SwiftUI Views

- Not applicable. Android demo coverage targets the Compose Multiplatform app shell.

## Atomic Design Components Used

- `ShellTextField`
- `ShellPrimaryButton`
- shared sidebar / rail / bottom-bar navigation

## Mock Data Used

- Demo auth credentials
- Demo documents and assistant knowledge corpus
- Demo sources, updates and dashboard metrics

## Data Flow

- Android instrumented test
- Compose semantics tags
- UI interactions
- ViewModels
- existing use cases and demo repositories

## Mermaid Diagram

- [[Android Demo Test Flow]]

## Development Notes

- Route-level tags are centralized in `DemoTestTags`.
- Recording pauses are configurable with `demoPauseMs`.
- Videos are recorded externally with `adb shell screenrecord` while the test runs.

## Open Questions

- Should the project later export recordings automatically from CI/emulators?
- Do we want tablet-specific demo devices for richer `Sources` recordings?
