## Problem

Desktop and web targets were not fully responsive. Several screens used hardcoded `dp` widths
that did not adapt to viewport size, and the three layout breakpoints (`WIDE_LAYOUT_MIN_WIDTH_DP`,
`RAIL_LAYOUT_MIN_WIDTH_DP`, `CONTENT_WIDE_MIN_WIDTH_DP`) lived only in `WorkspaceShell.kt`
(`composeApp`), so feature modules could not reuse them and tended to invent their own fixed
values instead.

Audit findings (full list, for follow-up):

- `feature/assistant/.../AssistantScreen.kt:69` — fixed `.width(220.dp)` conversations panel (acceptable as sidebar, not changed).
- `feature/documents/.../DocumentsScreen.kt:29-35` — fixed explorer/attributes pane widths, no scaling.
- `feature/documents/.../DocumentEditorPanel.kt` — desktop multi-pane editor has no max-width guard for ultrawide.
- `feature/updates/.../UpdatesTable.kt`, `MetadataIssuesTable.kt`, `HealthyDocumentsTable.kt` — fixed column widths, only a binary `isWide` toggle, no column-hiding strategy for narrow web windows.
- `feature/settings/.../SettingsScreen.kt:76` — fixed `.width(180.dp)` rail (acceptable, not changed).
- `feature/auth/.../SignInScreen.kt:259` — fixed `.width(160.dp)` submit button on desktop.
- `feature/sources/.../SourcesScreen.kt` — single-column only, no wide-screen multi-column layout.

## Change

Added `core/designsystem/.../theme/ShellWindowSize.kt`:

- `ShellWindowSize` object: single source of truth for `RAIL_MIN_WIDTH_DP` (600), `WIDE_MIN_WIDTH_DP`
  (840), `ULTRAWIDE_MIN_WIDTH_DP` (1440), `MAX_CONTENT_WIDTH_DP` (1120), plus `isRail/isWide/isUltrawide`
  helpers.
- `Modifier.shellMaxContentWidth()` extension to cap a scrollable content column at a readable
  width on ultrawide desktop/web instead of letting it stretch edge-to-edge.

`WorkspaceShell.kt` constants (`WIDE_LAYOUT_MIN_WIDTH_DP`, `RAIL_LAYOUT_MIN_WIDTH_DP`) now delegate
to `ShellWindowSize` instead of duplicating the numbers, so app-level and design-system breakpoints
can't drift apart.

`SettingsScreen.kt` content column now applies `.shellMaxContentWidth()` — settings forms no longer
stretch unreadably wide on a 4K/ultrawide window.

## Why this scope, not more

Did not touch `DocumentEditorPanel`'s multi-pane editor, the `Updates*Table` column widths, or
`AssistantScreen`'s sidebar — those panels are legitimately multi-column/fixed-rail by design, and
forcing `shellMaxContentWidth()` onto them would have clipped intentional split-pane layouts
without verifying behavior in a running app. Wrong fix here is worse than no fix.

## Next iteration (pending)

- Apply `ShellWindowSize.isUltrawide` to `Updates*Table` to add a responsive column-hiding tier
  beyond the current binary `isWide`, instead of fixed dp columns.
- `SourcesScreen.kt`: evaluate multi-column `ConnectionRow` grid above `WIDE_MIN_WIDTH_DP`.
- `SignInScreen.kt:259`: replace fixed `.width(160.dp)` button with `widthIn(min/max)` so it scales
  with form width instead of a magic constant.
- Consider exposing `ShellWindowSize` via a `LocalShellWindowWidth` composition local so deeply
  nested components don't need `isWide`/`isRail` booleans threaded through every constructor.

Related: [[App Shell Safe Area Adaptation]]
