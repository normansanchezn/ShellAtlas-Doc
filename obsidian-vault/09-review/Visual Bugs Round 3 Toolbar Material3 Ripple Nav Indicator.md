## Scope

Round 3 follow-up to [[Visual Bugs Audit Bottom Bar Contrast i18n Double Click Material Design]]. Same user, same screenshots showing the round-2 fixes hadn't landed visually yet for some items and surfacing new, more specific complaints: bottom bar selector reusing the same amber as unrelated chips, bottom bar missing elevation/animation, every screen's topbar allegedly differing from AI Assistant's, three different yellows colliding on the Documents screen, buttons not matching Material 3, Documents title/subtitle centered and clipped, the Alerts tab selector and table misaligned, Dashboard cards "ugly."

This time the root cause for most of these was the **same shared component being structurally wrong**, not per-screen drift — fixing it once in `ShellScreenToolbar` fixes Documents/Alerts/Dashboard/Settings/Assistant simultaneously, by construction.

---

## 1 & "topbar discrepancy" & "title/subtitle centered and clipped" — ONE root cause, fixed

`ShellScreenToolbar` (the actual shared component every screen calls, confirmed again — AI Assistant was never structurally different) used a **centered** title/subtitle column squeezed between two `widthIn(min = 132.dp, max = 240.dp)` reserved gutters on *both* sides, present whether or not a screen passed `leadingContent`/`trailingContent`. A screen with a long subtitle and no trailing actions (Documents: "Browse, read and maintain ShellAtlas knowledge") had nowhere to grow — Material 3's actual `TopAppBar` title is **start-aligned**, not centered, specifically so it can use all available width up to the actions slot. The "AI Assistant looks right" perception was just that its copy happens to be short enough not to hit the same wall.

Rewrote [ShellScreenToolbar.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellScreenToolbar.kt) to the standard M3 layout: `leadingContent` (only takes space if actually passed) → title/subtitle `Column(Modifier.weight(1f))`, **start-aligned**, `maxLines = 1` + ellipsis as a backstop (no longer the only thing preventing clipping — it now actually has room) → `trailingContent` end-aligned. One fix, applies to every screen because it's the same `@Composable` instance.

---

## 2. "El selector del bottomBar debería ser diferente al chip, no tiene que ver con Shell" — fixed

Root cause was the **token**, not the alpha: the bottom bar's selected-pill background and the risk/tab/citation chips elsewhere (`UpdatesTable` risk chips, `DocumentationHealthTabRow`, `SourcesList` citation rank) all draw from the *same* `colors.surfaceSelected` (a translucent brand-amber wash). Reusing one token for two different UI roles — "this nav item is the current screen" vs. "this is a status/severity chip" — is exactly why they visually collide and why neither looks intentional.

Fixed by giving navigation selection its own neutral treatment, separate from the chip token:
- [WorkspaceBottomBar.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt), [WorkspaceRail.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt), [WorkspaceSidebar.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt) — selected-item background changed `colors.surfaceSelected` → `colors.surfaceSubtle` (neutral gray) in all three nav surfaces, for consistency across breakpoints. The brand cue now lives **only** in the icon/label color (`colors.accentText`, from round 2) — chips keep `surfaceSelected` for their own (different) role.
- Also caught and fixed a leftover from round 2: `WorkspaceRail.kt`'s `RailItem` content color was still raw `colors.brand` (missed in the previous pass because it's assigned to a local `contentColor` val instead of inline at the `tint =`/`color =` call site, so it didn't match that round's grep). Same low-contrast-on-light bug as everywhere else; now `colors.accentText`.

## 2b. Bottom bar missing elevation and animation — fixed

- **Elevation**: `WorkspaceBottomBar`'s `Row` had no shadow at all — content directly behind/above it had no visual separation, exactly the "se ve parte de la pantalla" report. Added `.shadow(elevation = ShellElevation.raised)`, matching what `ShellScreenToolbar` already does at the top of the screen.
- **Animation**: every nav surface (`WorkspaceBottomBar`, `WorkspaceRail`, `WorkspaceSidebar`) used `.clickable(indication = null)` — no ripple, no press feedback of any kind beyond the (slow, 200ms+) color crossfade that only differentiates *selected* from *unselected*, not "I just pressed this." See §4 — fixed as part of the broader ripple pass.

---

## 3. "Tres colores de amarillo diferentes que chocan" en Documents — fixed (missed in round 2)

Round 2's `colors.brand → colors.accentText` sweep covered Assistant, the nav shell, and Sources — it never reached `feature/documents`, which had the exact same bug independently:
- [ExplorerTreePanel.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/ExplorerTreePanel.kt) — selected tree-node icon+label tint/color, and the bookmarked-document row icon+label, both `colors.brand` (lines formerly 115, 121, 176, 183).
- [DocumentReaderPanel.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentReaderPanel.kt) — breadcrumb link segments, `colors.brand` (formerly line 242).

So the screen really did have ≥3 distinct ambers fighting: raw brand-yellow text (low-contrast, the bug), the `surfaceSelected` pale-yellow tint behind it, and `colors.brand.copy(alpha = 0.4f)` as a selected-card border in [DocumentsScreen.kt:261](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/DocumentsScreen.kt) (left as-is — a border stroke, not foreground text, not the same defect class). All foreground (text/icon) occurrences swapped to `accentText`.

Also re-ran the project-wide grep for any remaining `colors.brand` used as `tint =`/`color =` and caught one more: [ChatInputBar.kt](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatInputBar.kt)'s send-button icon. Fixed the same way. `ShellHealthRing`'s progress arc and `MermaidDiagram`'s connector glyph still use raw `brand` — still a deliberate call (decorative graphic strokes, not body text), not an oversight this time.

---

## 4. "Los botones no corresponden a Material Design 3" — fixed (this is the thing flagged-but-deferred in round 2 §6)

Round 2 asked for a decision before touching this; the user has now reiterated it twice, so: done. Every interactive atom in `core/designsystem` had `indication = null` — zero Material state-layer/ripple feedback by design, replaced only by a custom scale+color animation. That's the literal cause of "no tiene animación" and "no corresponde a Material Design 3": M3 requires a visible state layer on press, and there wasn't one.

Replaced `indication = null` with `androidx.compose.material3.ripple()` in all 8 places found:
[ShellPrimaryButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellPrimaryButton.kt) (ripple tinted `onBrand` since its fill is brand-yellow), [ShellGhostButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellGhostButton.kt), [ShellIconButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellIconButton.kt), [ShellCard.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellCard.kt), [ChatMessageBubble.kt](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatMessageBubble.kt) (unbounded ripple, it's a small circular copy icon), and the three nav surfaces from §2b.

The existing scale/color press animations were **kept**, not replaced — ripple layers on top of them, same as Material3's own components do (state layer + content color shift together). Nothing about the brand's custom feel changes; it now also gets the M3-required feedback layer it was missing.

**Still not done, explicitly:** this is ripple as *feedback*, not a wholesale "convert every custom atom to literal `androidx.compose.material3.Button`" migration. The custom shapes/sizes/colors (88dp min-width pill buttons, custom radii, etc.) are intentional brand styling and out of scope unless told otherwise — flag if "Material Design 3" was meant more literally (i.e. replace `ShellPrimaryButton` with `Button` + `ButtonDefaults`, `ShellCard` with `Card`, etc.), which is a much bigger, riskier rewrite I won't do on an assumption.

---

## 5. Alerts tab selector misaligned + table below it — fixed (tab row), partially explained (table)

**Tab row** ([DocumentationHealthTabRow.kt](../../feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/DocumentationHealthTabRow.kt)): had no `fillMaxWidth()`, no `Arrangement`, and each segment was a bare `Text` with `clickable` directly on it — no `maxLines`/`softWrap` control. "Documentation Healthy" (the longest label) wrapped to 2 lines while the other two stayed on 1, which is what skewed the whole row's vertical alignment in the screenshot. Fixed: `Row.fillMaxWidth()` with `Arrangement.spacedBy`, each segment now a `Box(Modifier.weight(1f))` wrapping a centered, `maxLines = 1` + ellipsis `Text` — three equal-width segments, none of them able to wrap and throw off the row's height.

**Table** (`UpdatesTable`'s `HeaderCell`): already fixed in [[Alerts Rename Token Sessions And Brand Mark Swap]] (`maxLines = 1, softWrap = false, overflow = Ellipsis` added there). If the screenshot still shows "D / O / C / U..." stacked vertically letter-by-letter, that specific rendering is **not possible** with that fix applied — `softWrap = false` makes character-wrapping structurally unreachable. That strongly suggests the screenshot was taken against a build from before that commit landed, not a new bug. If it's still reproducible after a clean rebuild, that needs a fresh screenshot — I'm not re-diagnosing a bug I have direct code evidence shouldn't exist anymore on a guess.

---

## 6. Dashboard cards "se ven horribles" — NOT fixed, flagged as a design call

Looked at `ShellMetricCard` and the dashboard's mobile-width metric grid: at `isWide = false` the layout drops to a hard-coded single column (`columns = if (isWide) 3 else 1` in `DashboardScreen`), so on a narrow window each metric card is a nearly-empty full-width box (icon chip + one big number + one caption line) stacked vertically with a lot of dead space — which is what the screenshot shows. That's a sparse-layout problem, not a broken-component bug: the card itself renders correctly, it's just the wrong grid density for the available width.

Didn't change this. A real fix means picking a 2-column breakpoint between the current 1-and-3 cliff (or a more compact card variant for narrow widths), and that's a layout-density *design* decision, not a defect with one obvious correct answer — exactly the kind of change that needs a screenshot comparison before/after, which I don't have. Flagging precisely so it isn't lost, not silently skipping it.

---

## Verification

- `./gradlew :core:domain:desktopTest :feature:updates:desktopTest :feature:assistant:desktopTest :feature:settings:desktopTest :feature:documents:desktopTest :composeApp:desktopTest` — all green.
- `compileKotlinDesktop`, `compileDemoDebugKotlinAndroid`, `compileKotlinIosArm64`, `compileKotlinWasmJs` — clean (same pre-existing `LocalClipboardManager` deprecation warning as before, nothing new).
- Still no live UI run this session. Everything here is justified from direct code reading (component structure, color contrast math, layout constraints) — flagging again because it matters: if any of this still looks wrong after rebuilding, I need a *new* screenshot of the *current* build, not a re-diagnosis from the same evidence.

Related: [[Visual Bugs Audit Bottom Bar Contrast i18n Double Click Material Design]], [[Alerts Rename Token Sessions And Brand Mark Swap]]
