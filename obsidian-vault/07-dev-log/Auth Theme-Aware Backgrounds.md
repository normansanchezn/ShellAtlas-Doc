---
title: "Dev Log - Auth Theme-Aware Backgrounds"
type: "dev-log"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shellatlas
  - dev-log
  - auth
  - theming
---

# Dev Log - Auth Theme-Aware Backgrounds

## What changed

- Connected the persisted app theme to the sign-in screen instead of forcing a dark auth surface on mobile.
- Kept the existing animated node-and-star background for dark mode only.
- Added a separate animated light background built from warm gradients, soft glows and contour bands.
- Updated mobile auth copy colors to use the active theme rather than hardcoded white text.
- Removed the extra `Knowledge Platform` subtitle from the auth hero so the new brand mark carries the header alone.
- Softened desktop auth elevation with a larger, lower-opacity blur pass instead of a sharper card lift.
- Refined the light-mode background again with quieter contour spacing and lighter structural accents.

## Files created

- `obsidian-vault/07-dev-log/Auth Theme-Aware Backgrounds.md`
- `obsidian-vault/08-diagrams/Auth Theme Background Flow.md`

## Files modified

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/SignInScreen.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/ShellLoginBackground.kt`
- `obsidian-vault/03-features/Auth.md`
- `docs/project-tree.md`

## Decisions made

- The auth background now follows the saved theme choice, not a fixed dark aesthetic.
- Light mode gets its own visual language instead of reusing the dark-mode particle network.

## Issues found

- Mobile auth always wrapped itself in `ShellDocsTheme(darkTheme = true)`.
- The login canvas always painted a dark gradient, even when the app state was light.

## Tests added

- No new tests. Verified with desktop compilation of `feature:auth` and `composeApp`.

## Next steps

- If the team wants more control later, the auth surface can expose a dedicated appearance toggle that still writes
  through the same shared theme preference.
