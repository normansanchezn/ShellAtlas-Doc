---
title: "Documents"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
updated: 2026-06-12
tags:
  - shelldoc
  - documents
  - editor
---

# Documents

## Summary

Explorer, reader and editor flows for the shared documentation corpus, now with a dedicated creation screen, direct Supabase persistence fallback when the REST backend is not configured, and a title-driven new-document flow that no longer injects `Untitled document`.

## Related Files

- `core/domain/entity/document`
- `core/data/demo/DemoDocumentRepository.kt`
- `core/data/repository/SupabaseDocumentRepository.kt`
- `feature/documents`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/presentation/DocumentsViewModel.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`

## Layout

- `DocumentsScreen` now shows only one explorer tree on the left and the reader on the right. The redundant list pane was removed to avoid two competing trees/lists in the same workspace.
- Explorer tree and attributes/history rail start **collapsed** (`isExplorerExpanded`/`isAttributesExpanded = false`) whenever a document is selected, via `DocumentsViewModel.select()`. Each shows a slim `CollapsedPanelRail` when collapsed.
- Both panels are user-resizable by dragging a lighter `ResizeHandle` on their border. On desktop, the handle uses a horizontal resize cursor on hover. Widths clamp: explorer 180-420dp, attributes 200-360dp.
- Collapse affordances use neutral section icons instead of chevrons so they do not read like back-navigation. The collapsed rail is intentionally compact and low-contrast so it reads as a section toggle, not a primary button.
- Selecting a document auto-expands the explorer's ancestor folders (`DocumentNode.folderPathTo`) so the selected file is visible in the tree once expanded, and `ExplorerTreePanel` highlights it via `state.selectedDocument?.id`.
- Document list panel keeps its fixed width (250dp wide / 230dp narrow) — only the tree and attributes rail are collapsible/resizable.
- `+ New` no longer creates an immediate untitled row in-place. It opens `NewDocumentEditorPanel`, a dedicated full-screen creation flow with title + markdown + preview, separate from the normal reader workspace.
- The new-document flow keeps the markdown body empty by default; if the body is left blank on submit, `DocumentsViewModel` builds a heading from the title field the user typed, instead of hardcoding `Untitled document`.

## Open From Assistant

- Clicking a source card in the Assistant chat calls `AppNavigator.openDocument(documentId)`, which sets `openDocumentRequests` and navigates to `AppRoute.DOCUMENTS`.
- `DocumentsViewModel` collects `openDocumentRequests` in `init`, initializes if needed, selects the document (expanding tree path + collapsing side panels), then calls `consumeOpenDocumentRequest()`.

## Development Notes

- Search and browsing now remain in the KMM implementation only.
- Recovered Swift mock terminology is ported as demo content, not as a second UI implementation.
- `AppContainer` now prefers `SupabaseDocumentRepository` whenever Supabase is configured and the API gateway is not. Profile-specific config now comes from `SHELLDOC_DEV_*` / `SHELLDOC_PROD_*` variables instead of a single flat env key.
- `SupabaseDocumentRepository` persists `documents`, `document_versions`, `document_drafts` and `document_attributes` through PostgREST using the authenticated user's token and existing RLS policies.
- The creation editor and submit flow were updated after a regression where `# Untitled document` was leaking into the editor and colliding with the explicit title field.
- The visual density of panel separators and collapse rails was reduced to make the Documents workspace feel cleaner and less browser-like.
