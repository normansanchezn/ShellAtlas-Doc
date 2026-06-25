## Scope

Round 4. User pointed at two cropped screenshots (Alerts topbar vs AI Assistant topbar) side by side and said look at the top/horizontal separation — same component, visibly different framing. This time it was findable with certainty (not a guess): read every screen's call site of `ShellScreenToolbar` and compared.

## Root cause: only AI Assistant's toolbar bleeds full width; every other screen insets it

`ShellScreenToolbar` is `fillMaxWidth()` plus its own internal `.padding(horizontal = ShellSpacing.lg)` for the *text* — by design that's meant to span the full screen edge-to-edge as a real top-app-bar surface (background + `ShellElevation.raised` shadow reaching the true window edges), with only the title text inset from the bar's own edges.

`AssistantHeader` calls it correctly: `AssistantScreen`'s content `Column` has **no horizontal padding at all** (`Modifier.weight(1f).fillMaxHeight()`), so the toolbar's background/shadow rectangle genuinely touches the window edges.

Every other screen broke this, two different ways:
- **Documents** ([DocumentsScreen.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt)) and **Settings** ([SettingsScreen.kt](../../feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings/ui/SettingsScreen.kt)) passed `modifier = Modifier.padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md)` **directly on the `ShellScreenToolbar` call itself** — insetting the whole bar (background + shadow) by `lg` on both sides, on top of its own internal padding. The toolbar visually became a floating card with margins instead of a bar, with the shadow terminating in two vertical seams partway across the screen instead of fading out at the real edges.
- **Updates/Alerts** ([UpdatesScreen.kt](../../feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt)), **Dashboard** ([DashboardScreen.kt](../../feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/DashboardScreen.kt)), and **Sources** ([SourcesScreen.kt](../../feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources/ui/SourcesScreen.kt)) put the toolbar as the *first child inside* one big `Column` that had `.padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md)` applied for the whole screen (toolbar + tab row / metric cards / connection list, all together) — same inset bug, just inherited from the parent instead of applied directly.

This is exactly what showed up as "the separation looks different" between Alerts and Assistant: Alerts' bar doesn't reach the window edges, Assistant's does.

## Fix: toolbar always full-bleed, content gets its own padded scroll container below it

Restructured all five affected screens to match `AssistantScreen`'s actual shape:

```kotlin
Column(Modifier.fillMaxSize()) {
    ShellScreenToolbar(title = ..., subtitle = ..., trailingContent = { ... })   // no modifier — full bleed
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        // tab row / table / metric cards / connection list — unchanged otherwise
    }
}
```

- [DocumentsScreen.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt) — just dropped the toolbar's own `modifier` param (its content row below was never padded anyway, it manages its own resizable-pane layout).
- [SettingsScreen.kt](../../feature/settings/src/commonMain/kotlin/com/shelldocs/feature/settings/ui/SettingsScreen.kt) — same, dropped the modifier (its rail/content row already self-pads independently).
- [UpdatesScreen.kt](../../feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/UpdatesScreen.kt), [DashboardScreen.kt](../../feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/DashboardScreen.kt), [SourcesScreen.kt](../../feature/sources/src/commonMain/kotlin/com/shelldocs/feature/sources/ui/SourcesScreen.kt) — pulled the toolbar out of the single padded/scrollable `Column` into its own unpadded wrapper, with a second inner `Column` (same padding/spacing values as before) carrying everything that used to sit below it.

Net effect: all six screens (Documents, Settings, Alerts, Dashboard, Sources, AI Assistant) now produce byte-for-byte the same toolbar geometry — same component, same call shape, same surrounding structure, no per-screen modifier creeping back in.

## Verification

- `./gradlew :core:domain:desktopTest :feature:updates:desktopTest :feature:assistant:desktopTest :feature:settings:desktopTest :feature:documents:desktopTest :feature:dashboard:desktopTest :feature:sources:desktopTest :composeApp:desktopTest` — all green.
- `compileKotlinDesktop`, `compileDemoDebugKotlinAndroid`, `compileKotlinIosArm64`, `compileKotlinWasmJs` — clean.
- Found by reading every call site side by side, not by guessing from the screenshot pixels — this is the first round where I'm confident the screenshot and the explanation actually match. Still no live UI run; a fresh screenshot after rebuild is the real confirmation.

Related: [[Visual Bugs Round 3 Toolbar Material3 Ripple Nav Indicator]]
