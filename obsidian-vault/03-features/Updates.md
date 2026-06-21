---
title: "Updates"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-20
tags:
  - shelldoc
  - updates
  - health
  - metadata
---

# Updates

## Summary

Triage view over documents that need review or improvement, including the Documentation Health tables and the Metadata
Issues remediation flow.

## Purpose

Keep ShellAtlas documentation health consistent without allowing direct metadata acceptance from the table.

## Expected Behavior

- `Metadata Issues` exposes a single primary `Edit` action.
- `Edit` opens a targeted metadata dialog with only the flagged attributes.
- `Accept` inside the dialog prepares the reviewed values.
- `Confirm` applies the metadata update.
- The document disappears from `Metadata Issues` and only appears in `Documentation Healthy` when it passes validation.
- `Documentation Healthy` deduplicates rows by `documentId`.
- `AI Suggested Update` proposes markdown content changes, not documentation-health findings.
- If the document remains valid, the review screen shows `No documentation changes required.` and hides the publish
  action.

## Related Files

- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/UpdatesStringRes.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/UpdatesIntent.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/UpdatesViewModel.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/MetadataIssuesTable.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/EditMetadataDialog.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/ConfirmMetadataUpdateDialog.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/HealthyDocumentsTable.kt`
- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/entity/document/MetadataAssignment.kt`
-
`core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/classification/ApplyMetadataAssignmentsUseCase.kt`
- `core/domain/usecase/assistant/EvaluateDocumentHealthUseCase.kt`
- `core/data/repository/DerivedPendingUpdatesRepository.kt`

## Domain Models

- `PendingUpdate`
- `DocumentClassificationResult`
- `MetadataAssignment`
- `MetadataAttribute`

## Use Cases

- `GetPendingUpdatesUseCase`
- `GetMetadataIssuesUseCase`
- `GetHealthyDocumentsUseCase`
- `ApplyMetadataAssignmentsUseCase`
- `GenerateSuggestedUpdateUseCase`

## SwiftUI / Compose Views

- `UpdatesScreen`
- `MetadataIssuesTable`
- `EditMetadataDialog`
- `ConfirmMetadataUpdateDialog`
- `HealthyDocumentsTable`

## Atomic Design Components Used

- `ShellPrimaryButton`
- `ShellGhostButton`
- `ShellCard`
- `ShellDialog`
- `ShellBadge`

## Data Flow

`Metadata Issues table -> Edit dialog -> Accept draft values -> Confirm dialog -> ApplyMetadataAssignmentsUseCase -> DocumentClassificationRepository.assignMetadata -> reload Metadata Issues + Healthy Documents`

`Documentation Health row -> GenerateSuggestedUpdateUseCase -> related-document analysis -> minimal markdown update or no-change decision -> editor review -> publish pipeline`

## Mermaid Diagram

- `obsidian-vault/08-diagrams/Metadata Issues Review Flow.md`
- `obsidian-vault/08-diagrams/AI Suggested Update Generation Flow.md`

## Development Notes

- Health remains deterministic.
- Pending updates are derived from shared domain heuristics.
- Metadata edits are now document-scoped and confirmation-based.
- The updates module now keeps its touched UI copy in `UpdatesStringRes`.
- Suggested updates now preserve valid human-authored sections and only rewrite content when the document is too thin or
  structurally obsolete.
