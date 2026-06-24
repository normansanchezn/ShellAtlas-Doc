## Scope

Follow-up to [[Alerts Rename Token Sessions And Brand Mark Swap]]. User-reported, with screenshots: bottom-bar selected state invisible in light mode, design-system components reinvented per screen instead of reused, bottom-bar titles not localized and getting clipped, a pervasive "must click twice" bug, AI Assistant's topbar allegedly diverging from every other screen, and a general "you're breaking Material Design" call to bring every component back in line. Per request, this is the evidence document — fixes applied are listed with file:line; items not fixed are listed with why, not silently dropped.

---

## 1. Bottom bar selected state invisible in light mode — FIXED

**Root cause, not just "low alpha":** [WorkspaceBottomBar.kt:70](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt) (and the sidebar's equivalent, [WorkspaceSidebar.kt:173,240](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt)) used `colors.brand` (`#FFD100`, bright yellow) as the **text/icon foreground color** for the selected state. Bright yellow text on white has a contrast ratio close to 1:1 — functionally invisible. On dark backgrounds the same yellow has high contrast, which is why "funciona en dark mode" — it was never about `surfaceSelected`'s alpha being theme-aware, it was `brand` being unsafe as foreground color in light mode at all.

`brand` is correct as a **solid fill** (paired with `onBrand` per the existing `ShellPrimaryButton`), but it was also being reused as **foreground text/icon color directly on a flat surface** in 7 places — that's the actual defect class.

**Fix:**
- Added `ShellColorScheme.accentText` ([ShellColorScheme.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/tokens/ShellColorScheme.kt)): light = `#92400E` (amber-800, ~5.8:1 contrast on white), dark = `#FFD100` (unchanged — already safe there).
- Swapped every foreground (text/icon, not solid-fill-button) usage of `colors.brand` to `colors.accentText`:
  - [WorkspaceBottomBar.kt:70](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt)
  - [WorkspaceSidebar.kt:173, 240](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt)
  - [AssistantHeader.kt:55, 140](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantHeader.kt) (sparkles icon + knowledge-transfer icon)
  - [AssistantScreen.kt:149](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantScreen.kt) (empty-thread sparkles icon)
  - [ChatMessageBubble.kt:108](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatMessageBubble.kt) ("ShellAtlas AI" sparkles icon)
  - [AssistantRichContent.kt:159](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantRichContent.kt) (quick-reply chip text — was rendering brand-yellow text on a brand-yellow-12%-alpha chip, i.e. almost unreadable in *both* themes, not just light)
  - [SourcesList.kt:101](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/SourcesList.kt) — this is **"el elemento del puro ámbar"**: the citation rank number, rendered in `colors.brand` text inside an `colors.surfaceSelected` chip (a pale amber-on-amber-tint chip in light mode — effectively invisible).
- Also bumped `surfaceSelected` light-mode alpha `0x14 → 0x33` ([ShellLightColors.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/tokens/ShellLightColors.kt)) so the selected pill itself has a visible tint, not just the (now-fixed) text color carrying all the contrast.

**Not touched, documented only:** [ShellHealthRing.kt:40](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/ShellHealthRing.kt) (12dp progress-ring arc) and [MermaidDiagram.kt:197](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/molecules/MermaidDiagram.kt) (a "↓" connector glyph) still use `colors.brand` directly. These are graphic/decorative strokes, not body text competing for legibility the way the 7 fixed spots were — left as a judgment call rather than reflexively replacing every `colors.brand` reference. Flag if these should change too.

---

## 2. Design system: components reinvented per screen instead of reused

Concrete, file-and-line evidence, split into **fixed** and **found-but-not-fixed** (the latter needs a deliberate refactor pass, not a drive-by edit, because each one touches every screen that uses the pattern):

### Fixed this round
- **Two parallel, both-unlocalized string systems.** `composeApp/src/commonMain/kotlin/com/shelldocs/app/strings/StringRes.kt` duplicated what `core/designsystem/.../i18n/AppStrings.kt` already exists to do, and unlike `AppStrings` it was never localized — `WorkspaceSidebar` pulled `SEARCH_TXT`, `LIGHT_MODE_TXT`, `DARK_MODE_TXT`, `CONFLUENCE_TXT` straight from English-only constants regardless of app language. Deleted `StringRes.kt`; the 4 real strings (search placeholder, connections label, light/dark mode toggle label) plus 3 more that were hardcoded as bare string literals (`"Knowledge"`, `"Analytics"`, `"Sources"` section labels) moved into `AppStrings` with en/es/fr translations. See §3.
- **Right-click context menu** ([ExplorerTreePanel.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/ExplorerTreePanel.kt)) used raw `material3.DropdownMenu`/`DropdownMenuItem` instead of any shared styling — see [[Alerts Rename Token Sessions And Brand Mark Swap]] §5 for the `ShellContextMenu`/`ShellContextMenuItem` that now wraps it.
- **Tap-to-dismiss-keyboard gesture** was hand-written twice with the exact same bug (see §4) — consolidated into one `Modifier.clearFocusOnOutsideTap()` in designsystem, used by both call sites.

### Found, not fixed (needs its own pass — listed so it isn't lost)
- **Three separate hand-rolled "selectable nav item" implementations**, each re-deriving its own press/selection visuals instead of sharing one component:
  - `SidebarItem` in [WorkspaceSidebar.kt:158-213](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceSidebar.kt)
  - the rail item in [WorkspaceRail.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt)
  - the bottom-bar item inline in [WorkspaceBottomBar.kt:62-110](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt)

  All three independently: create their own `remember { MutableInteractionSource() }`, animate `surfaceSelected`/`textSecondary`↔accent color by hand, and apply `indication = null`. A bug fixed in one (like the contrast bug just fixed in all three individually, by hand, in three places) has to be fixed three times. A shared `ShellNavItem(icon, label, selected, badgeCount, onClick)` atom in designsystem, parameterized for the rail's icon-only mode vs the sidebar's icon+label mode vs the bottom bar's stacked mode, would collapse this to one implementation. Not done here — three different layouts (horizontal row, icon-only rail, stacked column) make this a real design decision (what does the shared API look like?), not a mechanical extraction.
- **`ShellDropdown.kt`** ([core/designsystem/.../atoms/ShellDropdown.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellDropdown.kt)) wraps the *same* unstyled `material3.DropdownMenu` that `ExplorerTreePanel`'s context menu used to — i.e. the exact bug just fixed in §5 of the previous doc still exists here, in the risk/version picker used by `UpdatesTable`. Not swapped to `ShellContextMenu` this round because `ShellContextMenu`'s API (single dismissable list of action items) doesn't currently fit a single-select field with a `selected` highlight state — would need its own variant, not a copy-paste.

---

## 3. Bottom bar not localized + label clipping — FIXED

Two separate bugs that looked like one:

1. **Not localized at all.** `AppRoute.title` (`"AI Assistant"`, `"Documents"`, `"Alerts"`, ...) is a plain English string, and both `WorkspaceSidebar` and `WorkspaceBottomBar` rendered it directly — switching the app to Spanish never touched the nav labels, the sidebar section headers ("Knowledge"/"Analytics"/"Sources"), or the search placeholder/theme-toggle label, because none of them ran through `AppStrings`/`LocalAppStrings` at all.

   Fix: `AppRoute.title` is now explicitly documented as a **stable English identifier only** (test tags, `DemoTestTags.navRoute`, accessibility) and is no longer used for any on-screen text. Added `AppRoute.label(strings: AppStrings)` ([AppRoute.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/navigation/AppRoute.kt)) and 6 new `navXxx` fields on `AppStrings` (en/es/fr) for the nav labels, plus `sidebarSearchPlaceholder`, `sidebarConnections`, `sidebarLightMode`, `sidebarDarkMode`, `sidebarSectionKnowledge/Analytics/Sources`. Both bars now call `route.label(strings)`.

2. **Clipping was a layout bug, not a wrapping/translation bug.** `WorkspaceBottomBar`'s nav item was `Modifier.width(64.dp)` — a fixed width regardless of how many items or how wide the phone actually is. On a 5-item bar split across, say, a 390dp-wide screen, 64dp/item leaves no headroom for anything beyond ~6-7 characters at the caption font size, so "Dashboard"/"Documents" clipped.

   Fix: changed the fixed `width(64.dp)` to `weight(1f)` ([WorkspaceBottomBar.kt](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceBottomBar.kt)) so each item gets an equal share of the *actual* available width instead of a hardcoded guess, and added `maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis` as a backstop so a label that still doesn't fit on a very narrow device ellipsizes cleanly instead of wrapping to two lines or clipping mid-character. Also dropped the old `route.title.substringBefore(' ')` hack (which is what produced "Documen[t]" truncation in the first place — it wasn't even using the wrapping/ellipsis system, it was manually chopping the string at the first space).

**Other hardcoded-English spots not in scope here** (found while auditing, not fixed): every feature module has its own unlocalized `*StringRes` object (`UpdatesStringRes`, `DocumentsStringRes`, `AssistantStringRes`, etc.) — table headers, button labels, empty-state copy, dialog text. None of it runs through `AppStrings`. Switching to Spanish today only translates Settings, Login, and (as of this fix) the nav shell — every feature screen's body content stays in English. This is a large, deliberate i18n rollout (translate + restructure N feature modules), not a bug fix; flagging it here because it's the same root cause as items just fixed, at a much bigger scale.

---

## 4. "Tengo que hacer doble clic" — root cause found and fixed at 3 confirmed sites

**Mechanism:** `detectTapGestures(onTap = ...)` and `clickable`/`combinedClickable` both listen on `PointerEventPass.Main` by default. When a `detectTapGestures` gesture is attached to a **container Box that wraps clickable children** (used here for "tap outside a text field to dismiss the keyboard/clear focus"), it competes with the children's own click handling on the same pass. The specific, reproducible failure mode: tap a clickable element while it's the *first* interaction in that gesture arena — the outer detector's coroutine and the child's coroutine both schedule against the same pointer event, and depending on which one wins the race, the first tap can get "spent" without the child's `onClick` firing, requiring a second tap.

This matches the symptom report exactly: it wasn't one broken widget, it was every clickable item *underneath* one of these wrapping tap-detectors — different widget types, same shared ancestor pattern.

**Found two such wrapping detectors, both fixed the same way:**
- [App.kt:78-80 (before fix)](../../composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt) — wraps the *entire* workspace (sidebar, bottom bar, every screen). This is the most likely single explanation for "Field" and most other reports, since it's the outermost ancestor of literally everything.
- [AssistantScreen.kt:84-89 (before fix)](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantScreen.kt) — wraps just the message thread (the box containing `LazyColumn` of chat bubbles, source chips, etc.). Likely explanation for the "puro ámbar" element needing two clicks, since that's a clickable chip living inside this exact Box.

**Fix:** new shared `Modifier.clearFocusOnOutsideTap()` ([ClearFocusOnOutsideTap.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/modifier/ClearFocusOnOutsideTap.kt)) replaces the raw `detectTapGestures` at both sites. It does the same job (clear focus / hide keyboard on an outside tap) but listens on `PointerEventPass.Final` and only fires if the pointer-up event reaches it **unconsumed**. Final runs strictly after Main, so any child's `clickable`/`combinedClickable` (Main pass) always gets first claim on the event — this detector can only ever react to taps nothing else wanted, by construction. It cannot regress anything that worked before (it's strictly more conservative than the old detector), and it removes the race entirely rather than just narrowing the window for it.

**Also found and fixed, a related-but-distinct issue:** [ExplorerTreePanel.kt](../../feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/ExplorerTreePanel.kt)'s tree-node row stacked **two independent pointer-input gesture detectors on the same node** — `combinedClickable(onClick, onLongClick)` *and* a raw `awaitPointerEventScope { while(true) { ... } }` loop layered on top via `.then(...)` to catch a desktop right-click. Two competing gesture coroutines on one node is the same class of bug as above, scoped to a single widget instead of a whole subtree — explains **"el elemento de los documentos en el Explorer."** Fixed by deleting the redundant raw listener; `onLongClick` (already wired to open the delete menu) works identically on touch, mouse-held-press, and is the only gesture handler on the node now. Trade-off: desktop/web lose *instant* right-click — context menu now opens on press-and-hold like everywhere else — in exchange for removing the only proven double-handler conflict in the codebase. If instant right-click is wanted back, it needs a *single* consolidated gesture detector (handling primary + long-press + secondary-click together in one `awaitEachGesture`), not a second detector bolted on.

**What I did not chase:** "el elemento Field" — there's no single `TextField`-adjacent wrapping gesture detector I could find beyond the two above; if double-click on a specific field persists after this fix, it's most likely explained by the App.kt-level fix (every field sits under that root Box) and should re-test there first. I did not go file-by-file looking for more instances beyond grepping for the literal anti-pattern (`combinedClickable`/`clickable` + `pointerInput` co-occurring in the same file) — that search returned exactly the Explorer file above and nothing else, but it would miss a wrapping detector several Composables removed from its victim (like the two `detectTapGestures` cases, which I only found by reading, not grepping). If the bug is still reproducible after this fix, it needs to be caught live (which screen, which exact element, OS) rather than guessed at again.

---

## 5. "AI Assistant topbar implementado distinto a las demás pantallas"

Checked: [AssistantHeader.kt](../../feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantHeader.kt) already calls the shared `ShellScreenToolbar` — same component `DocumentsScreen`, `SettingsScreen`, `UpdatesScreen` use. There is no structural divergence in the current code.

Most likely explanation: the screenshots in the report were taken **before** the topbar-elevation fix from [[Alerts Rename Token Sessions And Brand Mark Swap]] landed, or before this round's `accentText` contrast fix — the assistant topbar's sparkles icon and availability/knowledge-transfer buttons were rendering in low-contrast `colors.brand` (§1), which could visually read as "this toolbar looks different" purely because its accent elements were nearly invisible while other toolbars (which don't have brand-colored icons in their leading/trailing slots) looked "more normal" by comparison. That's now fixed in §1.

If a real visual discrepancy is still visible after rebuilding, it needs a fresh screenshot — I don't have a live instance to compare side-by-side this session, and I'm not going to restructure a component that's already correctly shared without concrete evidence of what's actually different.

---

## 6. "Debes apegarte a Material Design"

This needs a decision, not a unilateral fix. The codebase's `core/designsystem/atoms/*` (`ShellPrimaryButton`, `ShellGhostButton`, `ShellIconButton`, `ShellCard`) **deliberately** opt out of Material's ripple/state-layer system:

```kotlin
.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
```

at [ShellPrimaryButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellPrimaryButton.kt), [ShellGhostButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellGhostButton.kt), [ShellIconButton.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellIconButton.kt), [ShellCard.kt](../../core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellCard.kt), and three more in the nav-item files from §2. Every one of these substitutes a hand-built `scale + background-color` press animation (`ShellMotion.pressedScale`, `colors.surfaceSelected`) for Material's default ripple. This is consistent and intentional across the whole atoms layer — it's the chosen brand feel (the Figma redesign referenced throughout `ShellColorScheme`'s doc comments), not an accident or an oversight in one place.

Per literal Material Design guidelines this *is* a violation (no visible state layer on touch). But "fix" here means one of two very different things, and I'm not picking one without you:
- **(a)** Keep the custom press feel (it's already consistent app-wide, just had the §1 contrast bug) and treat "Material Design compliance" as already-intentionally-not-the-goal — ShellAtlas has its own design system, modeled on Material's structure (elevation tokens, spacing scale, color roles) but not its motion/feedback layer.
- **(b)** Actually adopt Material's ripple/indication everywhere, which means removing `indication = null` from 8 files and re-deriving the custom color/scale animations as a `LocalIndication` override instead of a bespoke per-component implementation — a real, app-wide visual change to how every button/card/nav-item feels when pressed.

I did not do (b) — it would touch every interactive atom in the design system on a guess about which way you want it to look, with no live app to compare before/after. Tell me which direction and I'll execute it as its own pass.

---

## Verification

- `./gradlew :core:domain:desktopTest :feature:updates:desktopTest :feature:assistant:desktopTest :feature:settings:desktopTest :feature:documents:desktopTest :composeApp:desktopTest` — all green.
- `compileKotlinDesktop`, `compileDemoDebugKotlinAndroid`, `compileKotlinIosArm64`, `compileKotlinWasmJs` — clean across all touched modules (no new errors; one pre-existing deprecation warning in `ChatMessageBubble.kt` re: `LocalClipboardManager`, unrelated to this pass).
- Not run in a live UI this session — no running desktop/web instance available. The contrast and double-click fixes are based on direct code/contrast-math evidence (cited above), not visual confirmation; the next person to pick this up should screenshot before closing it out.

Related: [[Alerts Rename Token Sessions And Brand Mark Swap]], [[Responsive Layout Breakpoints Consolidation]]
