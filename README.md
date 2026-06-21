# ShellAtlas

Kotlin Multiplatform implementation of **ShellEnterpriseDoc** — an enterprise knowledge platform
with an embedded, documentation-grounded AI assistant. One codebase, four targets:
**Android · iOS · Desktop (JVM) · Web (Wasm)**.

This repository keeps **KMM as the only product source of truth**. Useful logic recovered from a parallel Swift recovery was ported into the shared KMM modules, but the duplicated Swift app tree is not part of the maintained product anymore.

The UI is a pixel-faithful implementation of the
[Enterprise Knowledge Management Redesign](https://www.figma.com/file/vZNYrld1B75oIubmMiL6mt)
Figma file (light and dark palettes, Shell yellow `#FFD100`, 4dp radii, Inter type scale).

## Features

| Screen            | What it does                                                                                                                                                                                                                 |
|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **AI Assistant**  | Grounded Q&A over indexed docs: explains flows/processes step by step, decides when a document *should not* be improved, cites sources with relevance %, caches answers. Local LLM (Ollama) optional with graceful fallback. |
| **Documents**     | Explorer tree → document list → reader with attributes rail; split Markdown editor with live preview, autosaved drafts, publish, version history and restore.                                                                |
| **Notifications** | Maintenance triage computed from real document health: Critical/High/Medium/Low risk, age, impact and owner.                                                                                                                 |
| **Dashboard**     | Knowledge health ring, module coverage, status donut, AI usage chart, recent activity and needs-attention banners — derived from the live corpus.                                                                            |
| **Sources**       | Confluence / Azure DevOps / Jira integrations with sync, reconnect and the sync activity log.                                                                                                                                |
| **Settings**      | General (theme), AI Assistant, **Team & Access** (role delegation), Notifications, Integrations.                                                                                                                             |
| **Auth**          | Email/password sign-in via Supabase GoTrue. The AUTH feature is the reference clean-architecture example.                                                                                                                    |

## Demo Knowledge Corpus

The bundled demo data now includes Shell mobile process terminology such as:

- `EoSB1 Process for America's App - Android`
- `Lokalise Strings Update Process`
- `Azure Secrets Management for Mobile`

This keeps search, assistant grounding and updates triage aligned with the recovered ShellAtlas concept while staying fully deterministic.

## Roles

Roles are delegated through the Supabase **`user_roles`** table
(`supabase/migrations/0001_identity_and_roles.sql`) and enforced twice:
in the domain layer (`RolePermissions`) and by RLS policies.

| Role | Capabilities |
| --- | --- |
| `owner` | Everything: content, members, integrations, analytics, deletes |
| `develop` | View/edit/publish docs, assistant, analytics, run syncs |
| `business` | View docs, assistant, analytics (read-only content) |
| `viewer` | View docs (fallback for unassigned users) |

## Architecture

Clean Architecture + **MVI**, SOLID, one declaration per file.

```
composeApp            app shell, adaptive navigation, manual DI (AppContainer)
core/
  common              MVI base (MviViewModel/Intent/State/Effect), DomainResult, dispatchers
  domain              entities, repository interfaces, use cases (pure Kotlin)
  data                Supabase (GoTrue + PostgREST via Ktor), /v1 documents API client,
                      Markdown parser, AI engines (Grounded / Ollama / Composite), demo seed
  designsystem        Figma tokens, icons, atoms & molecules (Compose Multiplatform)
feature/
  auth · assistant · documents · updates · dashboard · sources · settings
      presentation/   State + Intent + Effect + ViewModel  (compose-free, unit tested)
      ui/             Compose screens & components
iosApp                SwiftUI host (Xcode project)
supabase/migrations   roles, documents, assistant cache (with RLS)
```

The MVI loop: `UI → Intent → ViewModel.handleIntent → setState/sendEffect → UI`.
Every feature follows the AUTH example: View → ViewModel → UseCase → Repository interface →
data-source implementation.

Recovered Swift-only logic that proved useful was absorbed into this structure, mainly:

- semantic alias expansion for search and assistant grounding
- ShellAtlas-specific demo corpus terminology
- documentation and architecture decisions restored into `obsidian-vault/`

## Running

```bash
./gradlew :composeApp:run                          # Desktop
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web
./gradlew :composeApp:assembleDebug                # Android
open iosApp/iosApp.xcodeproj                       # iOS (run the iosApp scheme)
```

Without configuration the app boots in **demo mode** (seeded in-memory data, any valid
credentials sign in as the workspace owner). Runtime configuration is now normalized through
`AppEnvironment`:

- `DEV` or `PROD` are resolved on every target.
- `DEV` prefers local URLs and local API endpoints.
- `PROD` prefers release URLs and release tokens.

The shared example file is:

```bash
cp .env.example .env
```

Minimum local setup:

```bash
SHELLDOC_APP_ENVIRONMENT=DEV
SHELLDOC_DEV_SUPABASE_URL=http://127.0.0.1:54321
SHELLDOC_DEV_SUPABASE_ANON_KEY=your-local-anon-key
SHELLDOC_DEV_API_BASE_URL=http://127.0.0.1:8787
```

Platform loaders:

- Android reads Gradle `BuildConfig` values and uses `dev` / `prod` flavors.
- Desktop reads `.env` or process env.
- iOS reads process env from the Xcode scheme or archive configuration.
- Web/Wasm reads query parameters from the app URL.

If those values are absent, `DEV` stays in demo mode and accepts any valid email plus an
8+ character password. `PROD` expects release config and fails fast if the backend endpoints
are missing.

On Android emulators, `127.0.0.1` / `localhost` are rewritten automatically to `10.0.2.2`
so the app can reach the host machine's local services.

Android release bundles are split by flavor:

```bash
./gradlew :composeApp:bundleDevRelease
./gradlew :composeApp:bundleProdRelease
```

Desktop releases should ship with an `.env` or launcher-supplied environment values.
iOS releases should ship with the same variables in the scheme or archive configuration.
Web releases should inject the variables in the deployment URL or host config.

Project documentation and ADRs live in `obsidian-vault/` and `docs/project-tree.md`.

## Supabase Local

Start the local stack with:

```bash
supabase start
```

Pull the linked remote schema with:

```bash
supabase db pull
```

Important notes:

- The repo keeps the hand-authored migrations in `0001_*`, `0002_*` and `0003_*` as the canonical local schema.
- The previous remote snapshot migration `20260612044949_remote_schema.sql` was neutralized because it dropped `assistant_intelligence`, `profiles`, `roles` and `user_roles` after they had already been created locally, which caused `supabase start` to fail during `seed.sql`.
- `supabase/seed.sql` now seeds only project-owned `public` tables. It intentionally does not import `auth`, `storage` or other Supabase-managed internal schemas from a raw dump.
- If Docker is running and `supabase start` still fails, rerun with `--debug` to inspect the exact migration or seed statement that broke bootstrap.

## Android Demo Videos

Instrumented demo walkthroughs live in [composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt](/Volumes/Mac%20mini%20extended/Development/KMM/ShelEnterpriseDoc/composeApp/src/androidInstrumentedTest/kotlin/com/shelldocs/app/demo/ShellAtlasDemoTest.kt:1).

Useful commands:

```bash
./gradlew :composeApp:connectedDevDebugAndroidTest
./gradlew :composeApp:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.shelldocs.app.demo.ShellAtlasDemoTest#demo_authAssistantAndDashboardWalkthrough
./gradlew :composeApp:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.shelldocs.app.demo.ShellAtlasDemoTest#demo_documentsEditAndPublishWalkthrough -Pandroid.testInstrumentationRunnerArguments.demoPauseMs=2200
./gradlew :composeApp:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.shelldocs.app.demo.ShellAtlasDemoTest#demo_sourcesSyncAndReconnectWalkthrough -Pandroid.testInstrumentationRunnerArguments.recordDemoVideo=true
```

The demo tests can now save two artifacts automatically:

- a final `.png` snapshot in the app's external pictures directory
- an optional `.mp4` per flow when `recordDemoVideo=true`

Video files are written on-device to:

```text
/sdcard/Movies/ShellAtlasDemo/
```

Snapshots are written on-device to:

```text
Android/data/com.shelldocs.app.dev/files/Pictures/shellatlas-demo/
```

If you prefer host-side capture, you can still record manually with `adb`:

Example:

```bash
adb shell screenrecord /sdcard/shellatlas-demo.mp4
adb pull /sdcard/shellatlas-demo.mp4
```

Important note:

- Paparazzi is great for screenshot regression, but it does not generate end-to-end demo videos.
- For ShellAtlas flows, instrumented Compose tests + `screenrecord` are the right tool for video artifacts.

## Tests

Unit tests cover the domain, data and presentation layers (use cases, role matrix,
health/improvement heuristics, retrieval, Markdown parser, Supabase/API clients via
MockEngine, and every feature ViewModel):

```bash
./gradlew test            # all targets' unit tests
```

127 tests, 0 failures at the time of the migration commit.
