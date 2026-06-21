---
title: "Dev Log - Desktop Logo Packaging And Sidebar Branding"
type: "dev-log"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - dev-log
  - branding
  - desktop
---

# Dev Log - Desktop Logo Packaging And Sidebar Branding

## What changed

- Centered the ShellAtlas brand block at the top of the workspace sidebar.
- Removed the `Knowledge Platform` subtitle from the sidebar header.
- Increased the rail brand chip size so compact and wide shells share the same visual anchor.
- Added shared SVG logo assets and wired the macOS desktop package icon to a generated `.icns`.
- Replaced the remaining prominent in-app legacy brand surfaces with a shared `ShellBrandBadge` backed by a shared
  `IconShellAtlasBrand` vector, including auth and assistant entry points.

## Files created

- `composeApp/src/commonMain/resources/drawable/shell_atlas_icon.svg`
- `composeApp/src/commonMain/resources/drawable/shell_atlas_logo.svg`
- `composeApp/src/desktopMain/resources/icons/ShellAtlas.icns`
- `obsidian-vault/07-dev-log/Desktop Logo Packaging And Sidebar Branding.md`
- `obsidian-vault/08-diagrams/Brand Asset Distribution Flow.md`

## Files modified

- `composeApp/build.gradle.kts`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/strings/StringRes.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellBrandBadge.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/icons/IconShellAtlasBrand.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantRichContent.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/SignInScreen.kt`
- `docs/project-tree.md`
- `obsidian-vault/03-features/Workspace View Hierarchy.md`

## Decisions made

- The sidebar keeps the brand concise: mark plus product name only.
- Packaged desktop branding uses a dedicated app icon file instead of relying on runtime window title only.

## Issues found

- Desktop native distributions were not setting any package icon.
- The sidebar and rail were using a smaller legacy brand treatment than the installable desktop app should expose.

## Tests added

- No new tests. Verification is done through desktop compilation and resource packaging checks.

## Next steps

- If Windows and Linux installers become first-class deliverables, add `.ico` and `.png` package icons alongside the
  current macOS `.icns`.
