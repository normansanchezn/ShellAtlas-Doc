---
title: "Dev Log - Documents Title Field And Markdown Creation"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - documents
---

# Dev Log - Documents Title Field And Markdown Creation

## What changed

- Removed the `Untitled document` default from the new-document editor flow.
- New documents now keep the markdown body empty by default.
- If the body is still blank on submit, `DocumentsViewModel` now builds a heading from the title field the user typed.
- `MviViewModel` now serializes intent handling through a shared mutex so fast back-to-back UI actions do not interleave out of order.
- Added a regression test that asserts the created document uses the typed title.

## Files created

- `obsidian-vault/07-dev-log/Documents Title Field And Markdown Creation.md`

## Files modified

- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/presentation/DocumentsViewModel.kt`
- `feature/documents/src/commonTest/kotlin/com/shelldocs/feature/documents/presentation/DocumentsViewModelTest.kt`
- `core/common/src/commonMain/kotlin/com/shelldocs/core/common/mvi/MviViewModel.kt`
- `obsidian-vault/03-features/Documents.md`

## Decisions made

- The title field remains the source of truth for the document heading.
- The body editor should not inject a placeholder title that conflicts with the explicit title input.
- Serializing intents in the shared MVI base reduces race conditions across the app, not only in Documents.

## Issues found

- None in the implementation itself.
- `./gradlew test` still reports an unrelated pre-existing `ExperimentalTime` test compilation issue in `core:domain`.

## Tests added

- `submitNewDocumentUsesTypedTitle`

## Next steps

- Consider whether the assistant-generated create-document flow should also standardize its initial body on the same helper.
