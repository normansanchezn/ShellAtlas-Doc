---
title: "Assistant Source Navigation And Documents Layout Resize"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - assistant
  - documents
---

# Assistant Source Navigation And Documents Layout Resize

## What changed

- `AppNavigator` gained `openDocumentRequests: StateFlow<String?>`, `openDocument(documentId)` (sets the request and navigates to `AppRoute.DOCUMENTS`), and `consumeOpenDocumentRequest()`.
- `SourcesList`/`SourceRow` and `ChatMessageBubble` source cards are now clickable, threading `onSourceClick(AnswerSource)` down from `AssistantScreen`'s new `onOpenDocument: (String) -> Unit` param.
- `WorkspaceShell` wires `AssistantScreen(onOpenDocument = container.navigator::openDocument)`.
- `DocumentsViewModel` gained `openDocumentRequests`/`consumeOpenDocumentRequest` constructor params (defaulted, so existing tests are unaffected) and an `init` collector that initializes (if needed), selects the requested document, and consumes the request.
- `DocumentsState` gained `isExplorerExpanded`/`isAttributesExpanded` (default `true`); new intents `ToggleExplorerPanel`/`ToggleAttributesPanel`.
- `select()` now expands the explorer tree path to the selected document (`DocumentNode.folderPathTo`) and collapses both side panels (`isExplorerExpanded = false`, `isAttributesExpanded = false`).
- New `feature/documents/ui/PanelControls.kt`: `ResizeHandle` (4dp drag divider via `detectDragGestures`) and `CollapsedPanelRail` (28dp strip + expand icon button).
- `DocumentsScreen` rewritten: explorer tree and document list are fixed/resizable side panels, reading pane gets `weight(1f)` (remaining space). Explorer width clamps 180-420dp (default 230dp), attributes width clamps 200-360dp (default 240dp), both `remember`-ed and adjusted via `ResizeHandle`.
- `DocumentReaderPanel` gained `attributesWidth: Dp` and `onResizeAttributes: (Float) -> Unit` params; replaced the old fixed-width attributes/history rail with collapse-aware `when` branching (`CollapsedPanelRail` when `!isAttributesExpanded`).

## Why

Per [[Assistant]] and [[Documents]]: clicking a source reference in the assistant's answer should jump straight to that document, selected in the explorer. The Documents layout previously gave equal weight to explorer/list/reader/attributes, making the document content (the most important part) hard to read with no way to resize. Now the reader gets the remaining space by default, and the tree/attributes panels start collapsed but stay reachable via collapse rails and drag-resize handles.

## Verification

- `./gradlew :feature:documents:compileKotlinDesktop :feature:assistant:compileKotlinDesktop :composeApp:compileKotlinDesktop --console=plain` -> BUILD SUCCESSFUL.
- `./gradlew :composeApp:run` -> launches cleanly, demo sign-in succeeds, no runtime exceptions.

## Follow-ups

- Manual interactive verification (clicking a source card end-to-end, dragging both resize handles, expanding collapsed rails) wasn't done in-session — recommend a quick pass next time the desktop app is run interactively.
