---
title: "Dev Log - App Shell Safe Area Adaptation"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - app-shell
  - mobile
---

# Dev Log - App Shell Safe Area Adaptation

## What changed

- Applied `WindowInsets.safeDrawing` at the app root so inset-sensitive devices do not render core content under notches or system bars.
- Kept the workspace adaptation in `Documents`, but moved the real safe-area fix to the shell level.

## Files created

- `obsidian-vault/07-dev-log/App Shell Safe Area Adaptation.md`

## Files modified

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`
- `obsidian-vault/02-architecture/KMM Architecture Overview.md`

## Decisions made

- Safe area should be enforced at the root composable, not by individual feature screens.
- `safeDrawing` is the right default for mobile and desktop because it is neutral where no insets exist.

## Issues found

- None in the build. The change compiled on Android after the root inset padding was added.

## Tests added

- None. This is a layout/system-inset change.

## Next steps

- Verify the root inset handling visually on a device with a notch and on iPad in both portrait and landscape.
