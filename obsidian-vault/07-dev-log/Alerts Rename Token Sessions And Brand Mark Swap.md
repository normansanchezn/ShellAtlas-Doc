## Requests handled this iteration

1. Rename "Documentation Health" screen to "Alerts"; give every topbar a visible elevation.
2. Triage table headers ("APP VERSION", "DOC VERSION", "LAST REVIEW"...) didn't fit their columns.
3. Remove the manual "Scan now" button; refresh on app open instead.
4. Token-based session persistence: surviving an app close if the token hasn't expired yet.
5. Replace the raw Material `DropdownMenu` context menu (looked like stock Android) with a themed one.
6. Replace the Shell Atlas wordmark/icon artwork with `shell-icon.svg`.

## 1. Alerts rename + topbar elevation

- `AppRoute.UPDATES` title: `"Notifications"` → `"Alerts"` ([AppRoute.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/navigation/AppRoute.kt)) — this drives both the sidebar label and `DemoTestTags.navRoute`.
- `UpdatesStringRes.PAGE_TITLE`: `"Documentation Health"` → `"Alerts"` ([UpdatesStringRes.kt](../../feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/UpdatesStringRes.kt)). Tab labels inside the screen (`TAB_HEALTH = "Documentation Health"`, etc.) were left as-is — those describe the tab content, not the screen.
- Elevation added once, at the source: [ShellScreenToolbar.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellScreenToolbar.kt) now has `.shadow(elevation = ShellElevation.raised)`. Every screen (Documents, Settings, Updates/Alerts, Dashboard, Sources) uses this shared toolbar, so this one change covers "all topbars" without touching each screen.
- Android instrumented test (`ShellAtlasDemoTest.kt`) updated: `navigateTo("Notifications")` → `navigateTo("Alerts")`.

## 2. Table header overflow

Root cause: `HeaderCell` in `UpdatesTable.kt` / `MetadataIssuesTable.kt` / `HealthyDocumentsTable.kt` had no `maxLines`/`overflow`, so two-word headers ("APP VERSION") wrapped to two lines inside their fixed-width columns and threw off row alignment. Fixed by adding `maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis` to all three `HeaderCell` composables — same fix, three call sites, no column-width changes needed.

## 3. Scan now → auto-refresh on open

- `UpdatesIntent.ScanNow` removed; `UpdatesIntent.Initialize` now calls what used to be `scan()` (was previously a separate, cheaper `load()` path). The screen refreshes via `pendingUpdatesRepository.scanNow()` every time it's opened instead of waiting for a manual click.
- Dead code removed as a result: `UpdatesState.isLoading` (only ever set by the old `load()`), the `LOADING_PENDING_UPDATES` string, the `GetPendingUpdatesUseCase` injection into `UpdatesViewModel` (still used elsewhere, e.g. `AppContainer.pendingUpdatesCount()` for the sidebar badge — only the *viewmodel's own copy* was unused), and `DemoTestTags.UpdatesScan`.
- Tests: `UpdatesViewModelTest` — `ScanNow` references swapped to `Initialize`; Android instrumented walkthroughs no longer click a now-nonexistent button.

## 4. Token-based session persistence

Previously `SupabaseAuthRepository.restoreSession()` just returned the **in-memory** `mutableSession.value` — there was no real persistence. `AppAuthRepository` (the wrapper in `composeApp`) only ever stored a boolean `session_active` flag, used solely by `DemoAuthRepository`'s `initiallyLoggedIn` flag. On a real Supabase relaunch the user was always sent back to the login screen, even mid-token-lifetime.

Now:

- `AuthSession` (full token + expiry + profile) is serialized to JSON and stored via a new `SessionPreferences.loadAuthSessionToken()/saveAuthSessionToken()` pair — implemented identically across all four platform stores (`Preferences` on desktop, `SharedPreferences` on Android, `localStorage` on web, `NSUserDefaults` on iOS). See [AuthSessionPersistence.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/AuthSessionPersistence.kt) for the (de)serialization.
- `AuthRepository` interface gained `fun adoptSession(session: AuthSession)` — lets a wrapper hydrate a concrete repo's in-memory `StateFlow` from a persisted session. Implemented by `SupabaseAuthRepository`, `DemoAuthRepository`, and the test `FakeAuthRepository`/`StubAuthRepository` fixtures.
- `AppAuthRepository.restoreSession()`: tries the delegate's own in-memory session first (demo mode keeps working unchanged); otherwise loads the persisted token, and **only accepts it if `expiresAt` is still in the future** — otherwise it clears the stale token and falls through to the login screen. No network refresh-token call was added (Supabase GoTrue's refresh-token grant isn't wired up yet) — this satisfies exactly what was asked: stay logged in across a relaunch *as long as the token hasn't expired*, nothing more.
- `AppContainer.runStartupDiagnostics()` now calls `authRepository.restoreSession()` before flipping `isAppLaunching = false`, so `App.kt`'s `session == null` check (which decides login vs. workspace) sees the restored session on the very first frame.

### Follow-up (not done — needs a product decision first)

- No refresh-token flow: once `expiresAt` passes, the user is logged out even with a valid `refreshToken` sitting in storage. Wiring `POST /auth/v1/token?grant_type=refresh_token` into `SupabaseAuthApi` would let `AppAuthRepository.restoreSession()` attempt a silent refresh before giving up — left out here because it's a meaningfully bigger change (new API call, error handling, token rotation) than "don't log out on relaunch."
- Persisted token storage is plaintext (`Preferences`/`SharedPreferences`/`localStorage`/`NSUserDefaults`), matching how the existing `session_active` flag was already stored. If this needs to be hardened (Keychain/EncryptedSharedPreferences), that's a separate, deliberate security pass — flagging it here rather than silently downgrading scope.

## 5. Themed context menu (was: "looks Android")

The right-click delete menu on a document tree node ([ExplorerTreePanel.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/ExplorerTreePanel.kt)) used raw `androidx.compose.material3.DropdownMenu`/`DropdownMenuItem` with Material's default surface color, shape, and elevation — visually inconsistent with the rest of the app's custom-drawn `ShellCard`-style chrome, most noticeable on desktop/web where Material's defaults don't match the OS chrome either.

Added [ShellContextMenu.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellContextMenu.kt): `ShellContextMenu` + `ShellContextMenuItem` wrap the same underlying `DropdownMenu`/`DropdownMenuItem` (Compose doesn't give you a from-scratch popup without reimplementing focus/dismiss handling) but force `ShellTheme` colors, `ShellRadius.md` shape, a `ShellTheme.colors.border` outline, and `ShellElevation.overlay` shadow — matching `ShellCard`. Swapped into `ExplorerTreePanel`.

### Follow-up

- `ShellDropdown.kt` (the field-style select used in `UpdatesTable` risk/version pickers) has the *same* unstyled `DropdownMenu` underneath. Not touched this round — it wasn't the button the user flagged, and changing it touches every screen with a dropdown field, which deserves its own pass + visual check rather than a drive-by edit.

## 6. Brand mark swap

`IconShellAtlasLogo` (full wordmark ImageVector, 521×253 viewBox) and `IconShellAtlasBrand` (icon-only mark, 250×233 viewBox) were both hand-transcribed from old SVGs and used in exactly two places: `WorkspaceSidebar`'s `SidebarHeader` and `ShellBrandBadge`. Both deleted, along with the source `shell_atlas_logo.svg`/`shell_atlas_icon.svg` files (unreferenced by any build script — they were just leftover source art, not loaded into the build).

New [IconShellIcon.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/icons/IconShellIcon.kt) transcribes `composeApp/src/commonMain/resources/drawable/shell-icon.svg` (two paths: red shell outline, yellow ray pattern; viewBox 122.88×113.76) as an `ImageVector`, same pattern as the icon it replaced (the project hand-converts SVGs to `ImageVector.Builder` rather than using Compose Resources — there's no `org.jetbrains.compose.resources` plugin wired up, so `painterResource`/`Res.drawable` aren't available here).

- `ShellBrandBadge` now renders `IconShellIcon` directly (1:1 swap, same usage as before).
- `SidebarHeader`: the old `IconShellAtlasLogo` was a full wordmark (icon + "Shell Atlas" lettering baked into the SVG paths). The new asset is icon-only, so `SidebarHeader` was changed from a single full-width `Icon` to a `Row` of `IconShellIcon` (32dp) + a `Text("Shell Atlas")` using `ShellTheme.typography.pageTitle`, to keep the brand name visible in the sidebar without re-hand-tracing a wordmark that doesn't exist in the new asset.

### Follow-up

- `IconShellPecten.kt` is an unrelated generic placeholder shell glyph (hardcoded geometric paths, not brand art) used in fallback monochrome chips — left alone, out of scope for this request.
- If a real wordmark (icon + "Shell Atlas" lettering as one asset) is wanted later, it needs to be supplied as a new SVG — the lettering in the old `IconShellAtlasLogo` was specific to that mark's geometry and not reusable with `shell-icon.svg`'s paths.

## Verification

- `./gradlew :core:domain:desktopTest :feature:updates:desktopTest :feature:assistant:desktopTest :feature:settings:desktopTest :feature:documents:desktopTest :composeApp:desktopTest` — all green.
- `:feature:auth:desktopTest` has one pre-existing failing test (`canSubmitRequiresBothFields`) confirmed via `git stash` to fail identically on `master` before this iteration's changes — not a regression, not touched here.
- Compiled `compileKotlinDesktop`, `compileDemoDebugKotlinAndroid`, `compileKotlinIosArm64`, `compileKotlinWasmJs` across all touched modules — no errors (pre-existing deprecation warnings only).
- UI not manually clicked through in a running app this round (no running desktop/web instance available in this session) — flagging per verification discipline rather than claiming visual confirmation I don't have.

Related: [[Responsive Layout Breakpoints Consolidation]], [[Assistant Source Navigation And Documents Layout Resize]]
