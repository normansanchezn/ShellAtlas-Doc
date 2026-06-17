---
title: "Mobile Typography"
type: "feature"
status: "active"
platform: "Android/iOS"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - typography
  - mobile
  - accessibility
---

# Mobile Typography

## Summary

ShellDoc now uses a dedicated mobile typography scale instead of reusing the denser desktop/web baseline on phones. This raises readability, keeps hierarchy closer to native mobile apps, and avoids accessibility regressions caused by 10sp to 13sp defaults.

## Related Files

- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/tokens/ShellTypography.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/theme/ShellTheme.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`

## Expected Behavior

- Phones use `ShellTypography.mobile()`.
- Desktop and web keep the denser default scale.
- Keyboard zoom shortcuts remain desktop-only.
- Captions, labels, body text, titles and code blocks render at sizes more consistent with mobile accessibility expectations.

## Accessibility Notes

- Body text now starts at `16sp` on mobile.
- Labels now start at `14sp` on mobile.
- Captions now start at `12sp` on mobile instead of `10sp`.
- Code text now starts at `14sp` on mobile for better readability in editors and assistant/code blocks.
- The change is global, so accessibility improves consistently across Dashboard, Documents, Assistant, Settings and Sources.

## Mermaid Diagram

- `obsidian-vault/08-diagrams/Mobile Typography Flow.md`

## Development Notes

- The mobile typography variant is selected from `App` based on the adaptive layout width threshold already used by the shell.
- `ShellDocsTheme` now accepts a typography token set directly, which keeps future tablet or accessibility presets straightforward.
