---
title: "Dev Log - Documentation Health Metadata Issues UX Corrections"
type: "dev-log"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - dev-log
  - updates
  - metadata
---

# Dev Log - Documentation Health Metadata Issues UX Corrections

## What changed

- Removed the direct `Accept` action from the Metadata Issues table.
- Added a two-step edit flow: targeted metadata dialog with `Accept`, followed by a final confirmation dialog with
  `Cancel` and `Confirm`.
- Deduplicated Documentation Healthy rows by `documentId`.
- Tightened the Documentation Health tab chips and fixed healthy-table header spacing and action labels.
- Moved the touched updates-module copy into `UpdatesStringRes`.

## Files created

- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/entity/document/MetadataAssignment.kt`
-
`core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/classification/ApplyMetadataAssignmentsUseCase.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/UpdatesStringRes.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/ConfirmMetadataUpdateDialog.kt`
-
`core/domain/src/commonTest/kotlin/com/shelldocs/core/domain/usecase/classification/ApplyMetadataAssignmentsUseCaseTest.kt`
- `core/domain/src/commonTest/kotlin/com/shelldocs/core/domain/usecase/updates/GetHealthyDocumentsUseCaseTest.kt`
- `obsidian-vault/08-diagrams/Metadata Issues Review Flow.md`
- `obsidian-vault/07-dev-log/Documentation Health Metadata Issues UX Corrections.md`

## Files modified

- `docs/project-tree.md`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppContainer.kt`
- `core/data/src/commonMain/kotlin/com/shelldocs/core/data/repository/DerivedDocumentClassificationRepository.kt`
- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/updates/GetHealthyDocumentsUseCase.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/DocumentationHealthTab.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/UpdatesIntent.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/UpdatesViewModel.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/DocumentationHealthTabRow.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/EditMetadataDialog.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/HealthyDocumentsTable.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/MetadataIssuesTable.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt`
- `feature/updates/src/commonTest/kotlin/com/shelldocs/feature/updates/presentation/UpdatesViewModelTest.kt`
- `obsidian-vault/03-features/Updates.md`

## Decisions made

- Metadata application remains routed through Domain use cases instead of applying per-field updates directly from
  Compose UI.
- The edit dialog only renders attributes currently flagged by classification.
- Module-local string ownership is acceptable for `feature/updates` without forcing a repo-wide i18n migration in the
  same task.

## Issues found

- The existing async scan test assumed immediate scheduler timing and had to be stabilized with a small fake delay.

## Tests added

- `ApplyMetadataAssignmentsUseCaseTest`
- `GetHealthyDocumentsUseCaseTest`

## Next steps

- If you want the same string-centralization pattern elsewhere, the next logical modules are `feature/dashboard`,
  `feature/documents`, and `feature/assistant`.
