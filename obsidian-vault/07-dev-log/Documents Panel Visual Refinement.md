---
title: "Dev Log - Documents Panel Visual Refinement"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - documents
  - ui
---

# Dev Log - Documents Panel Visual Refinement

## What changed

- Reduced the visual weight of panel dividers in the Documents workspace.
- Added a desktop hover cursor for resize handles.
- Replaced chevron collapse affordances with neutral section icons, and tightened the collapsed rail so it reads as a panel toggle instead of a back action.
- Removed the redundant document list pane so the workspace only keeps the folder tree.
- Added a mobile-first fallback for narrow screens: Explorer and Reader now switch as separate panes, and both editor flows stack vertically instead of forcing a desktop split view.

## Files created

- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/PointerModifiers.kt`
- `feature/documents/src/androidMain/kotlin/com/shelldocs/feature/documents/ui/PointerModifiers.android.kt`
- `feature/documents/src/desktopMain/kotlin/com/shelldocs/feature/documents/ui/PointerModifiers.desktop.kt`
- `obsidian-vault/07-dev-log/Documents Panel Visual Refinement.md`

## Files modified

- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentReaderPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentEditorPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/PanelControls.kt`
- `obsidian-vault/03-features/Documents.md`

## Decisions made

- Keep a single explorer tree in the Files/Documents workspace.
- Treat collapse rails as section toggles, not back-navigation.
- Make resize affordances discoverable through hover state instead of thicker visual bars.

## Issues found

- Desktop hover cursors need an `expect/actual` split so Android builds do not depend on desktop-only pointer APIs.

## Tests added

- None. This is a UI refinement, not a business-logic change.

## Next steps

- Consider giving the collapse rails a softer hover background only on desktop.
