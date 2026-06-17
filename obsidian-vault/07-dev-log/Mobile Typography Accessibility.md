---
title: "Dev Log - Mobile Typography Accessibility"
type: "dev-log"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - dev-log
  - typography
  - mobile
  - accessibility
---

# Dev Log - Mobile Typography Accessibility

## What changed

- Added `ShellTypography.mobile()` as a dedicated mobile type scale.
- Updated `ShellDocsTheme` so it can receive the active typography token set.
- Updated `App` to choose mobile typography on narrow layouts instead of reusing desktop/web sizing.
- Limited keyboard zoom shortcuts to non-mobile layouts so phone typography stays predictable.

## Files modified

- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/tokens/ShellTypography.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/theme/ShellTheme.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`

## Decisions made

- Keep desktop/web unchanged and solve the issue at the theme boundary for mobile only.
- Raise body text to `16sp` and caption text to `12sp` on phones as a practical accessibility baseline.

## Tests added

- Re-ran `:composeApp:compileDemoDebugKotlinAndroid`.
- Re-ran the iOS simulator host build through `xcodebuild`.

## Next steps

- Review dense dashboard cards and bottom navigation labels on the smallest supported phone width.
