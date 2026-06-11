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

Explorer, reader and editor flows for the shared documentation corpus, now with a dedicated creation screen and direct Supabase persistence fallback when the REST backend is not configured.

## Related Files

- `core/domain/entity/document`
- `core/data/demo/DemoDocumentRepository.kt`
- `core/data/repository/SupabaseDocumentRepository.kt`
- `feature/documents`

## Layout

- `DocumentsScreen` is a `Row`: explorer tree -> document list -> reading pane (`weight(1f)`, gets remaining space) -> attributes/version-history rail.
- Explorer tree and attributes/history rail start **collapsed** (`isExplorerExpanded`/`isAttributesExpanded = false`) whenever a document is selected, via `DocumentsViewModel.select()`. Each shows a `CollapsedPanelRail` (28dp strip with expand icon) when collapsed.
- Both panels are user-resizable by dragging a `ResizeHandle` (4dp draggable divider, `feature/documents/ui/PanelControls.kt`) on their border. Widths clamp: explorer 180-420dp, attributes 200-360dp.
- Selecting a document auto-expands the explorer's ancestor folders (`DocumentNode.folderPathTo`) so the selected file is visible in the tree once expanded, and `ExplorerTreePanel` highlights it via `state.selectedDocument?.id`.
- Document list panel keeps its fixed width (250dp wide / 230dp narrow) — only the tree and attributes rail are collapsible/resizable.
- `+ New` no longer creates an immediate untitled row in-place. It opens `NewDocumentEditorPanel`, a dedicated full-screen creation flow with title + markdown + preview, separate from the normal reader workspace.

## Open From Assistant

- Clicking a source card in the Assistant chat calls `AppNavigator.openDocument(documentId)`, which sets `openDocumentRequests` and navigates to `AppRoute.DOCUMENTS`.
- `DocumentsViewModel` collects `openDocumentRequests` in `init`, initializes if needed, selects the document (expanding tree path + collapsing side panels), then calls `consumeOpenDocumentRequest()`.

## Development Notes

- Search and browsing now remain in the KMM implementation only.
- Recovered Swift mock terminology is ported as demo content, not as a second UI implementation.
- `AppContainer` now prefers `SupabaseDocumentRepository` whenever Supabase is configured but `SHELLDOC_API_BASE_URL` is not. This fixes the previous behavior where document changes looked saved in UI but still lived only in `DemoDocumentRepository`.
- `SupabaseDocumentRepository` persists `documents`, `document_versions`, `document_drafts` and `document_attributes` through PostgREST using the authenticated user's token and existing RLS policies.
