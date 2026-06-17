---
title: "Dev Log - Documents Mobile Editor Preview And Keyboard Fixes"
type: "dev-log"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - dev-log
  - documents
  - mobile
  - keyboard
---

# Dev Log - Documents Mobile Editor Preview And Keyboard Fixes

## What changed

- Split the mobile edit flow into two explicit stages: `Edit` and `Preview`.
- Removed live preview from the phone editor until the user taps `Continue to preview`.
- Reused the attributes dialog as a required step before preview opens.
- Moved the mobile continue action directly under the live editor field.
- Applied the same staged flow to `NewDocumentEditorPanel`, so document creation now goes source -> attributes -> preview -> create.
- Persisted new-document attributes immediately after creation so the saved record matches the previewed metadata.
- Added app-level tap-to-dismiss keyboard behavior.
- Set Android `windowSoftInputMode` to `adjustNothing` so the bottom bar stays under the keyboard while the editor content handles IME spacing.

## Files created

- `obsidian-vault/08-diagrams/Documents Mobile Editing Flow.md`
- `obsidian-vault/07-dev-log/Documents Mobile Editor Preview And Keyboard Fixes.md`

## Files modified

- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/presentation/DocumentsIntent.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/presentation/DocumentsState.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/presentation/DocumentsViewModel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentEditorPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/AttributesEditDialog.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`
- `feature/documents/src/commonTest/kotlin/com/shelldocs/feature/documents/presentation/DocumentsViewModelTest.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`
- `composeApp/src/androidMain/AndroidManifest.xml`
- `obsidian-vault/03-features/Documents.md`

## Decisions made

- Keep the desktop editor untouched because it is currently the most stable surface.
- Gate phone preview behind metadata capture instead of showing source, preview and attributes at once.
- Prefer a root-level focus clear for keyboard dismissal rather than duplicating that behavior screen by screen.

## Issues found

- The project still emits existing KMP warnings around deprecated Android-style test folders and deprecated `Instant` typealiases.
- The live editor auto-scroll currently optimizes for append-at-the-end writing, which matches the common case but may need refinement for complex mid-document edits.

## Tests added

- Added `continueToPreviewRequiresAttributesAndThenShowsPreview` in `DocumentsViewModelTest`.
- Re-ran `:feature:documents:testDebugUnitTest`.
- Re-ran `:composeApp:compileDemoDebugKotlinAndroid`.
- Re-ran the iOS simulator host build through `xcodebuild`.

## Next steps

- Validate the new mobile editor flow manually on Android and iPhone simulators with the on-screen keyboard.
- Manually validate the new-document preview flow on Android and iPhone simulators with long markdown drafts.
