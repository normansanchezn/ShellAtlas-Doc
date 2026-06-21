---
title: "Dev Log - Workspace Toolbar And Decision Button Consistency"
type: "dev-log"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - dev-log
  - ux
  - toolbar
---

# Dev Log - Workspace Toolbar And Decision Button Consistency

## What changed

- Added `ShellScreenToolbar` as the shared desktop/web page header.
- Migrated Dashboard, Assistant, Documents, Sources, Settings, Updates and AI Update screens to the same toolbar
  structure.
- Added feature-local string objects for touched modules to reduce new hardcoded copy in the migrated headers.
- Increased base decision button height and minimum width so confirm/cancel style actions render consistently across
  dialogs and editors.

## Files created

- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellScreenToolbar.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/AssistantStringRes.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/DashboardStringRes.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/DocumentsStringRes.kt`
- `feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings/SettingsStringRes.kt`
- `feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources/SourcesStringRes.kt`
- `obsidian-vault/03-features/Workspace View Hierarchy.md`
- `obsidian-vault/07-dev-log/Workspace Toolbar And Decision Button Consistency.md`
- `obsidian-vault/08-diagrams/Workspace Toolbar Hierarchy Flow.md`

## Files modified

- `docs/project-tree.md`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellGhostButton.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellPrimaryButton.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantHeader.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantScreen.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/DashboardScreen.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentEditorPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`
- `feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings/ui/SettingsScreen.kt`
- `feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources/ui/SourcesScreen.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/AiUpdateScreen.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt`

## Decisions made

- Toolbar consistency is enforced from the Design System instead of screen-by-screen tweaks.
- Page titles stay in the toolbar; panel labels remain below as local content hierarchy.
- Button normalization was handled in atoms so dialogs and editor flows inherit the fix automatically.

## Issues found

- The previous workspace had several valid layouts but no single source of truth for toolbar height or title placement.

## Tests added

- No new tests. Verified through metadata compilation of the touched modules.

## Next steps

- If the team wants stricter enforcement of the "max two items per side" rule, the next step is to model toolbar actions
  as typed slots instead of freeform composables.
