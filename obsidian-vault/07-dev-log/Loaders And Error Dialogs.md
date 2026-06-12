---
title: "Dev Log - Loaders And Error Dialogs"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - feedback
---

# Dev Log - Loaders And Error Dialogs

## What changed

- Added a shared modal progress dialog and a reusable feedback card surface.
- Replaced inline error text in interactive features with modal dialogs.
- Added blocking progress dialogs for strict async actions.
- Disabled action buttons while operations are in flight.

## Files created

- `core/common/.../ErrorDialogState.kt`
- `core/designsystem/.../ShellFeedbackCard.kt`
- `core/designsystem/.../ShellErrorDialog.kt`
- `core/designsystem/.../ShellLoadingOverlay.kt`
- `obsidian-vault/03-features/User Feedback Loaders and Error Dialogs.md`
- `obsidian-vault/08-diagrams/User Feedback Flow.md`
- `obsidian-vault/07-dev-log/Loaders And Error Dialogs.md`
- `obsidian-vault/06-decisions/ADR-005 Shared User Feedback for Async Actions.md`

## Files modified

- Auth, Dashboard, Documents, Assistant, Settings, Sources and Updates presentation/UI modules.
- `docs/project-tree.md`
- `ShellGhostButton`
- ViewModel tests for updated dialog-based failure state.

## Decisions made

- User-facing failures should be modal and descriptive.
- Error translation belongs in shared presentation/common code, not in repositories.
- Loaders should be consistent across screens to avoid per-feature reinvention.
- Placeholder states should share the same container so only the copy changes between loading, empty and error situations.

## Issues found

- Desktop common tests required explicit `ExperimentalTime` opt-in during validation.

## Tests added

- Existing ViewModel tests were updated to assert dialog state instead of raw inline error strings.

## Next steps

- Standardize success notices.
- Consider a shared non-blocking progress pattern for background refreshes.
