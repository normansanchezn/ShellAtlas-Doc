---
title: "Documents"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - documents
  - editor
---

# Documents

## Summary

Explorer, reader and editor flows for the shared documentation corpus.

## Related Files

- `core/domain/entity/document`
- `core/data/demo/DemoDocumentRepository.kt`
- `feature/documents`

## Layout

- `DocumentsScreen` is a `Row`: explorer tree -> document list -> reading pane (`weight(1f)`, gets remaining space) -> attributes/version-history rail.
- Explorer tree and attributes/history rail start **collapsed** (`isExplorerExpanded`/`isAttributesExpanded = false`) whenever a document is selected, via `DocumentsViewModel.select()`. Each shows a `CollapsedPanelRail` (28dp strip with expand icon) when collapsed.
- Both panels are user-resizable by dragging a `ResizeHandle` (4dp draggable divider, `feature/documents/ui/PanelControls.kt`) on their border. Widths clamp: explorer 180-420dp, attributes 200-360dp.
- Selecting a document auto-expands the explorer's ancestor folders (`DocumentNode.folderPathTo`) so the selected file is visible in the tree once expanded, and `ExplorerTreePanel` highlights it via `state.selectedDocument?.id`.
- Document list panel keeps its fixed width (250dp wide / 230dp narrow) — only the tree and attributes rail are collapsible/resizable.

## Open From Assistant

- Clicking a source card in the Assistant chat calls `AppNavigator.openDocument(documentId)`, which sets `openDocumentRequests` and navigates to `AppRoute.DOCUMENTS`.
- `DocumentsViewModel` collects `openDocumentRequests` in `init`, initializes if needed, selects the document (expanding tree path + collapsing side panels), then calls `consumeOpenDocumentRequest()`.

## Development Notes

- Search and browsing now remain in the KMM implementation only.
- Recovered Swift mock terminology is ported as demo content, not as a second UI implementation.
