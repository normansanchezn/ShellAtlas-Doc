---
title: "ADR-005 Shared User Feedback for Async Actions"
type: "decision"
status: "accepted"
created: 2026-06-11
tags:
  - adr
  - shelldoc
  - feedback
---

# ADR-005 Shared User Feedback for Async Actions

## Context

Async actions across ShellDoc had inconsistent feedback. Some flows changed state silently, while failures appeared as inline text and sometimes exposed technical wording.

## Decision

Use a shared feedback pattern:

- Progress is represented by screen-level loading overlays.
- Failures are translated from `AppError` into `ErrorDialogState`.
- Screens render failures through a shared modal dialog component.
- Interactive buttons are disabled while the related action is processing.

## Consequences

- Users always get visible feedback for long-running actions.
- Error copy becomes consistent and easier to understand.
- ViewModels carry a small amount of additional UI feedback state.

## Alternatives Considered

- Keeping inline error labels per screen.
- Letting repositories return preformatted user copy.
- Using snackbars for all failures, including blocking flows.
