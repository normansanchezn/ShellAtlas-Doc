---
title: "Dev Log - Persistence Contract Tests"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - testing
  - persistence
---

# Dev Log - Persistence Contract Tests

## What changed

- Added unit tests for `SupabasePostgrestApi` headers and REST URLs.
- Added a Supabase document repository test that verifies draft saves hit `document_drafts`.
- Added a remote API repository test that verifies the `/v1/documents/{id}/draft` path.
- Fixed the `core:data` test source set so the existing `kotlin.time`-based tests compile again.

## Files created

- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/supabase/SupabasePostgrestApiTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/repository/SupabaseDocumentRepositoryTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/repository/ApiDocumentRepositoryTest.kt`
- `obsidian-vault/07-dev-log/Persistence Contract Tests.md`
- `obsidian-vault/09-review/Persistence Communication Validation.md`

## Files modified

- `core/data/build.gradle.kts`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/assistant/GroundedAssistantEngineTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/demo/DemoDocumentRepositoryTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/demo/DemoSourcesRepositoryTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/mapper/DocumentDtoMapperTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/repository/DerivedDashboardRepositoryTest.kt`
- `core/data/src/commonTest/kotlin/com/shelldocs/core/data/repository/DerivedPendingUpdatesRepositoryTest.kt`

## Decisions made

- Validate local and remote persistence by contract tests around the repository layer, not by UI-driven flows.
- Keep the test coverage close to the transport adapters so failures are easier to attribute.

## Issues found

- The backend does not currently expose a testable Confluence client or a visible `/v1/sync/confluence` implementation in `backend/src/index.ts`.
- `core:data:desktopTest` still contains one unrelated failing assistant test outside this persistence work.

## Tests added

- `SupabasePostgrestApiTest`
- `SupabaseDocumentRepositoryTest`
- `ApiDocumentRepositoryTest`

## Next steps

- Add a testable Confluence sync adapter if the backend boundary is introduced.
- Decide whether the missing `/v1/documents/{id}/attributes` route in the backend should be implemented or removed from the client contract.
