---
title: "Dev Log - Android Demo Tests"
type: "dev-log"
created: 2026-06-16
updated: 2026-06-20
tags:
  - shellatlas
  - dev-log
  - android
  - testing
---

# Dev Log - Android Demo Tests

## What changed

- Added Android instrumented tests for demo walkthroughs.
- Added shared Compose test tags for sign-in, navigation and assistant input.
- Documented how to run the demo flows and record videos with `adb screenrecord`.
- Replaced `UiDevice` orientation control with Activity-managed landscape orientation.
- Added automatic final snapshots for each flow and optional on-device `.mp4` recording.
- Forced a newer Espresso version in `androidTest` to avoid the `InputManager.getInstance()` crash path from the older transitive stack.
- Replaced Compose-root snapshot capture with device `screencap` so animated screens no longer crash instrumentation
  while generating demo artifacts.
- Disabled auth background animation and assistant perpetual motion only during instrumented runtime so Compose reaches
  idle state for demo flows.

## Files created

- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt`
- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/DemoArtifacts.kt`
- `core/common/src/commonMain/kotlin/com/shelldocs/core/common/testing/DemoTestTags.kt`
- `obsidian-vault/03-features/Android Demo Tests.md`
- `obsidian-vault/08-diagrams/Android Demo Test Flow.md`
- `obsidian-vault/07-dev-log/Android Demo Tests.md`

## Files modified

- `composeApp/build.gradle.kts`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceShell.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/SignInScreen.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatInputBar.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellTextField.kt`
- `README.md`
- `docs/project-tree.md`

## Decisions made

- Keep recording outside the test process and make the UI flow deterministic inside the test.
- Use landscape orientation so phone-size devices can expose the wider workspace navigation.
- Prefer built-in Compose snapshots plus device `screenrecord` over Paparazzi for end-to-end demo flows.
- After the Compose layout crash was reproduced, stable device-level capture became the default snapshot strategy.

## Issues found

- The app needed stable semantics tags before Android UI automation would be reliable.
- The instrumented stack was resolving `espresso-core:3.5.0`, which is too old for the current emulator/device API combination that throws `NoSuchMethodException` on `InputManager.getInstance()`.
- The latest failing local run did not reproduce a `GetPendingUpdatesUseCase` class loading issue. It failed in
  Compose measurement while trying to capture the final snapshot.

## Tests added

- `demo_authAssistantAndDashboardWalkthrough`
- `demo_documentsBrowseHistoryAndBookmarkWalkthrough`
- `demo_documentsCreateWalkthrough`
- `demo_documentsEditAndPublishWalkthrough`
- `demo_updatesScanAndFilterWalkthrough`
- `demo_sourcesSyncAndReconnectWalkthrough`
- `demo_settingsSectionsAndSignOutWalkthrough`

## Next steps

- Run the suite on a connected emulator/device and pull the generated `.mp4` / `.png` artifacts.
- If screenshot regression becomes a priority, add Paparazzi separately for static states, not for video.
