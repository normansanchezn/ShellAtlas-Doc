# ShellDocs

Kotlin Multiplatform migration of **ShellEnterpriseDoc** — an enterprise knowledge platform
with an embedded, documentation-grounded AI assistant. One codebase, four targets:
**Android · iOS · Desktop (JVM) · Web (Wasm)**.

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

## Tests

Unit tests cover the domain, data and presentation layers (use cases, role matrix,
health/improvement heuristics, retrieval, Markdown parser, Supabase/API clients via
MockEngine, and every feature ViewModel):

```bash
./gradlew test            # all targets' unit tests
```

127 tests, 0 failures at the time of the migration commit.
