---
title: "Dev Log - Android Demo Tests"
type: "dev-log"
created: 2026-06-16
updated: 2026-06-16
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

## Files created

- `composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt`
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

## Issues found

- The app needed stable semantics tags before Android UI automation would be reliable.

## Tests added

- `demo_authAssistantAndDashboardWalkthrough`
- `demo_documentsAndUpdatesWalkthrough`
- `demo_sourcesAndSettingsWalkthrough`

## Next steps

- Run the suite on a connected emulator/device and capture the first demo clips.
- Add richer document editing flows if product demos need authoring scenarios too.
