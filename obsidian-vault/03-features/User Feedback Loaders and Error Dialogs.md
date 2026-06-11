---
title: "User Feedback Loaders and Error Dialogs"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - ux
  - feedback
  - dialogs
---

# User Feedback Loaders and Error Dialogs

## Summary

Shared feedback system for asynchronous user actions across Auth, Dashboard, Documents, Assistant, Sources, Updates and Settings.

## Purpose

Give clear progress feedback while the app is working and keep all user-facing failures in understandable dialogs instead of inline technical text.

## User Problem

Users could trigger saves, refreshes, scans, syncs or assistant requests without seeing whether the app was processing. When something failed, the UI showed raw inline messages that felt technical and easy to miss.

## Expected Behavior

- Every async action shows a visible loader.
- Every failure opens a modal dialog with product-language copy.
- Buttons are disabled while the related action is in progress.
- Inline error text is removed from feature screens.
- Interactive controls feel reliable: the whole button surface is clickable, cursor contrast is visible, and Enter submits high-intent forms like login and assistant chat.

## Related Files

- `core/common/src/commonMain/kotlin/com/shelldocs/core/common/error/ErrorDialogState.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellLoadingOverlay.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellErrorDialog.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant`
- `feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates`
- `feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings`

## Domain Models

- `AppError`
- `DomainResult`

## UseCases

- Document CRUD and history use cases
- Assistant question flow use cases
- Dashboard metrics retrieval
- Source sync and reconnect use cases
- Team member and sign-out use cases
- Pending updates scan use cases

## Repositories

- Existing repositories remain unchanged.
- Error presentation stays in presentation/common layers, not in repositories.

## ViewModels

- `AuthViewModel`
- `DashboardViewModel`
- `DocumentsViewModel`
- `AssistantViewModel`
- `SourcesViewModel`
- `UpdatesViewModel`
- `SettingsViewModel`

## SwiftUI Views

Not applicable in the current KMM implementation. The product uses Compose Multiplatform screens hosted by platform shells.

## Atomic Design Components Used

- `ShellGhostButton`
- `ShellPrimaryButton`
- `ShellDialog`
- `ShellLoadingOverlay`
- `ShellErrorDialog`

## Mock Data Used

- Existing demo repositories and in-memory sources only.

## Data Flow

`User Intent -> ViewModel -> UseCase -> DomainResult/AppError -> dialog/loading state -> Compose screen`

## Mermaid Diagram

Referenced note: `obsidian-vault/08-diagrams/User Feedback Flow.md`

## Development Notes

- `AppError` is translated to `ErrorDialogState` through a shared mapper.
- Long-running actions use a screen overlay so the user always sees that the request is still active.
- Assistant answering keeps the typing indicator and also gets dialog-based failure handling.
- `ShellPrimaryButton` and `ShellGhostButton` now center content and keep the full visible button area clickable.
- `ShellTextField` now exposes a stronger cursor color, focused border state and optional submit callback so login and single-line actions can react to Enter.

## Open Questions

- Should success notices also move to a shared dialog/snackbar policy?
- Should we add a non-blocking inline progress variant for long assistant generations later?
