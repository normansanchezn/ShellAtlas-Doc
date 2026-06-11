# ShellDocs

Kotlin Multiplatform implementation of **ShellEnterpriseDoc** — an enterprise knowledge platform
with an embedded, documentation-grounded AI assistant. One codebase, four targets:
**Android · iOS · Desktop (JVM) · Web (Wasm)**.

This repository keeps **KMM as the only product source of truth**. Useful logic recovered from a parallel Swift recovery was ported into the shared KMM modules, but the duplicated Swift app tree is not part of the maintained product anymore.

The UI is a pixel-faithful implementation of the
[Enterprise Knowledge Management Redesign](https://www.figma.com/file/vZNYrld1B75oIubmMiL6mt)
Figma file (light and dark palettes, Shell yellow `#FFD100`, 4dp radii, Inter type scale).

## Features

| Screen | What it does |
| --- | --- |
| **AI Assistant** | Grounded Q&A over indexed docs: explains flows/processes step by step, decides when a document *should not* be improved, cites sources with relevance %, caches answers. Local LLM (Ollama) optional with graceful fallback. |
| **Documents** | Explorer tree → document list → reader with attributes rail; split Markdown editor with live preview, autosaved drafts, publish, version history and restore. |
| **Updates Pending** | Maintenance triage computed from real document health: Critical/High/Medium/Low risk, age, impact and owner. |
| **Dashboard** | Knowledge health ring, module coverage, status donut, AI usage chart, recent activity and needs-attention banners — derived from the live corpus. |
| **Sources** | Confluence / Azure DevOps / Jira integrations with sync, reconnect and the sync activity log. |
| **Settings** | General (theme), AI Assistant, **Team & Access** (role delegation), Notifications, Integrations. |
| **Auth** | Email/password sign-in via Supabase GoTrue. The AUTH feature is the reference clean-architecture example. |

## Demo Knowledge Corpus

The bundled demo data now includes Shell mobile process terminology such as:

- `EoSB1 Process for America's App - Android`
- `Lokalise Strings Update Process`
- `Azure Secrets Management for Mobile`

This keeps search, assistant grounding and updates triage aligned with the recovered ShellDoc concept while staying fully deterministic.

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
- ShellDoc-specific demo corpus terminology
- documentation and architecture decisions restored into `obsidian-vault/`

## Running

```bash
./gradlew :composeApp:run                          # Desktop
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web
./gradlew :composeApp:assembleDebug                # Android
open iosApp/iosApp.xcodeproj                       # iOS (run the iosApp scheme)
```

Without configuration the app boots in **demo mode** (seeded in-memory data, any valid
credentials sign in as the workspace owner). Point it at real services by passing an
`AppConfig(supabase = SupabaseConfig(...), api = ApiConfig(...), useOllama = true)` to `App()`.

For the desktop app you can also use a root `.env` file:

```bash
cp .env.example .env
```

Set at least:

```bash
SHELLDOC_SUPABASE_URL=http://127.0.0.1:54321
SHELLDOC_SUPABASE_ANON_KEY=your-local-anon-key
```

If those values are absent, the app stays in demo mode and accepts any valid email plus an
8+ character password.

On Android emulators, `127.0.0.1` / `localhost` are rewritten automatically to `10.0.2.2`
so the app can reach the host machine's local Supabase services.

Project documentation and ADRs live in `obsidian-vault/` and `docs/project-tree.md`.

## Tests

Unit tests cover the domain, data and presentation layers (use cases, role matrix,
health/improvement heuristics, retrieval, Markdown parser, Supabase/API clients via
MockEngine, and every feature ViewModel):

```bash
./gradlew test            # all targets' unit tests
```

127 tests, 0 failures at the time of the migration commit.
