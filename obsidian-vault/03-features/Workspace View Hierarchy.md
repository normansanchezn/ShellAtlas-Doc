---
title: "Workspace View Hierarchy"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - app-shell
  - ux
  - toolbar
---

# Workspace View Hierarchy

## Summary

Shared workspace screens now use one consistent top toolbar height and title hierarchy for desktop and web layouts, and
the sidebar brand block now uses the full ShellAtlas logo without extra subtitle or duplicated brand text.

## Purpose

Prevent page titles, panel titles and decision actions from competing visually across Dashboard, Assistant, Documents,
Sources, Settings and Updates.

## User Problem

- Page-level headers were implemented independently in each feature.
- Split layouts could make panel titles feel like page titles.
- Confirm/cancel style actions were not guaranteed to keep a consistent button footprint.

## Expected Behavior

- Desktop and web screens render the same toolbar height.
- The page title stays centered in the top bar.
- The sidebar and rail top brand mark stay visually aligned with the packaged desktop app icon.
- No more than two controls should be placed on the left or right toolbar areas.
- Secondary panel labels stay inside the content area and no longer compete with the page-level title.
- Decision buttons keep the same base height and minimum width.

## Related Files

- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellScreenToolbar.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellPrimaryButton.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellGhostButton.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantHeader.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/DashboardScreen.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentEditorPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`
- `feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings/ui/SettingsScreen.kt`
- `feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources/ui/SourcesScreen.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/AiUpdateScreen.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/strings/StringRes.kt`
- `composeApp/src/commonMain/resources/drawable/shell_atlas_icon.svg`
- `composeApp/src/commonMain/resources/drawable/shell_atlas_logo.svg`
- `composeApp/src/desktopMain/resources/icons/ShellAtlas.icns`
- `composeApp/build.gradle.kts`

## Domain Models

- No domain changes required.

## UseCases

- No use case changes required.

## Repositories

- No repository changes required.

## ViewModels

- Existing view models remain unchanged; the hierarchy fix is UI-only.

## SwiftUI Views

- Not applicable in this KMM implementation.

## Atomic Design Components Used

- Molecule: `ShellScreenToolbar`
- Atoms: `ShellPrimaryButton`, `ShellGhostButton`
- App shell branding: `WorkspaceSidebar`, `WorkspaceRail`

## Mock Data Used

- Existing feature state only. No new mock data.

## Data Flow

Toolbar strings and state are provided by each feature screen. The shared toolbar enforces layout and title placement
while the screen content keeps its local panel structure below it.

## Mermaid Diagram

- [[Workspace Toolbar Hierarchy Flow]]

## Development Notes

- Documents now gets a page-level toolbar before the explorer/reader split, so `Explorer`, breadcrumbs and document
  titles remain at lower hierarchy levels.
- Assistant no longer uses a custom header height that diverges from the rest of the workspace.
- Button sizing was normalized at the atom level to reduce inconsistent confirm/cancel rendering.
- Sidebar branding now removes the `Knowledge Platform` subtitle and the duplicated `ShellAtlas` text label so the
  full logo lockup can stand on its own without competing with the page hierarchy.

## Open Questions

- If the app later needs breadcrumbs at the page level, they should be added inside `ShellScreenToolbar` instead of
  per-feature ad hoc headers.
