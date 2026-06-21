---
title: "Android Demo Tests"
type: "feature"
status: "active"
platform: "Android"
area: "ShellAtlas"
owner: "Product Engineering"
created: 2026-06-16
updated: 2026-06-20
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
- Each flow can save a final snapshot automatically without depending on Compose root image capture.
- Each flow can save an `.mp4` automatically when `recordDemoVideo=true`.

## Related Files

- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt`
- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/DemoArtifacts.kt`
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
- Instrumented flows now force landscape via the Activity instead of depending on `UiDevice`.
- Final snapshots are now saved through device-level `screencap`, which avoids Compose measurement crashes while the
  UI is still animating.
- Demo videos can be saved automatically to `/sdcard/Movies/ShellAtlasDemo/`.
- The latest local failure mode was not `GetPendingUpdatesUseCase` missing from the APK. The reproducible crash path
  came from snapshot capture during active layout/animation work.
- The auth background and assistant perpetual animations now disable themselves only during instrumented runtime so
  Compose idling stays stable.
- Paparazzi was intentionally not used for video capture because it is a screenshot tool, not an end-to-end recorder.

## Open Questions

- Should the project later export recordings automatically from CI/emulators?
- Do we want tablet-specific demo devices for richer `Sources` recordings?
