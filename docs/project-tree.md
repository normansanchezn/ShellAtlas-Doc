# ShellDoc Project Tree

Updated: 2026-06-11

## Overview

ShellDoc now uses the Kotlin Multiplatform codebase as the only product implementation. The prior Swift recovery was treated as a temporary recovery source for logic and documentation, not as a second app to keep in the repository.

Primary architecture:

```text
UI (Compose/SwiftUI host) -> Presentation (MVI) -> Domain <- Data
```

## Repository Root

```text
ShelEnterpriseDoc/
├── composeApp/                      ← Main multiplatform app shell
│   ├── src/androidMain/             ← Android entrypoints
│   ├── src/commonMain/              ← Shared app container + navigation
│   ├── src/desktopMain/             ← Desktop host
│   ├── src/iosMain/                 ← iOS bridge
│   └── src/wasmJsMain/              ← Web/Wasm host
├── core/
│   ├── common/                      ← Dispatchers, errors, MVI primitives, result types
│   ├── domain/                      ← Entities, repository interfaces, use cases
│   ├── data/                        ← Demo data, repositories, network clients, assistant engines
│   └── designsystem/                ← Tokens, icons, atoms and molecules
├── feature/
│   ├── assistant/                   ← Assistant state + Compose UI
│   ├── auth/                        ← Sign-in flow
│   ├── dashboard/                   ← Health and activity metrics
│   ├── documents/                   ← Explorer, reader and editor flows
│   ├── settings/                    ← Preferences and integrations
│   ├── sources/                     ← Mock / future external sources
│   └── updates/                     ← Pending updates triage
├── iosApp/                          ← SwiftUI host project for the shared KMM app
├── supabase/                        ← SQL migrations and local snippets
├── backend/                         ← Future backend helpers and markdown tooling
├── docs/
│   ├── architecture.md              ← KMM architecture notes
│   ├── glossary/
│   └── project-tree.md
├── obsidian-vault/                  ← Project brain and decision log
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Core Modules

### `core/common`

```text
core/common/src/commonMain/kotlin/com/shelldocs/core/common/
├── coroutines/                      ← Dispatcher abstractions
├── error/                           ← Shared app/domain errors
├── id/                              ← ID generation helpers
├── mvi/                             ← MviIntent, MviState, MviEffect, MviViewModel
├── result/                          ← DomainResult helpers
└── time/                            ← TimeProvider abstractions
```

### `core/domain`

```text
core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/
├── entity/
│   ├── assistant/                   ← Assistant answers, health, grounding, improvement decisions
│   ├── auth/                        ← User, roles, permissions, session
│   ├── dashboard/                   ← Dashboard metrics and activity models
│   ├── document/                    ← Document aggregate, attributes, versions
│   ├── source/                      ← Source integrations and sync logs
│   └── updates/                     ← Pending update risk models
├── repository/                      ← Source-of-truth interfaces for data access
└── usecase/
    ├── assistant/                   ← Intent detection, retrieval, health, assistant orchestration
    ├── auth/                        ← Sign in/out and role checks
    ├── dashboard/                   ← Metrics retrieval
    ├── document/                    ← CRUD, search and versions
    ├── source/                      ← Sources and sync actions
    └── updates/                     ← Pending review scans
```

### `core/data`

```text
core/data/src/commonMain/kotlin/com/shelldocs/core/data/
├── assistant/                       ← Grounded, Ollama and composite assistant engines
├── demo/                            ← DemoSeed and in-memory repositories
├── mapper/                          ← DTO to domain mappers
├── markdown/                        ← Markdown parsing and hashing
├── network/                         ← Future API client for documents
├── repository/                      ← Derived repositories and caches
└── supabase/                        ← Future auth/profile adapters
```

### `core/designsystem`

```text
core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/
├── atoms/
├── icons/
├── molecules/
├── theme/
└── tokens/
```

## Feature Modules

```text
feature/<name>/src/commonMain/kotlin/com/shelldocs/feature/<name>/
├── presentation/                    ← State, intent, effect, view model
└── ui/                              ← Compose screens and UI sections
```

Current feature responsibilities:

- `assistant`: grounded Q&A, improvement guidance, source citations.
- `auth`: demo and future real auth entry flow.
- `dashboard`: health, coverage, activity and attention views.
- `documents`: explorer, reader, editor and version history.
- `settings`: theme, AI and integration preferences.
- `sources`: mock integrations and sync state.
- `updates`: triage of outdated or risky documents.

## Removed Duplicate Structure

The following recovered Swift-only structure is no longer part of the product architecture and should not be recreated:

```text
DS-Core/
SD-Domain/
SD-Data/
SD-DesignSystem/
SD-Presentation/
ShellDoc.xcworkspace
ShellDoc/
```

Any reusable behavior from that recovery must be ported into the KMM modules above instead of restoring a second app tree.
