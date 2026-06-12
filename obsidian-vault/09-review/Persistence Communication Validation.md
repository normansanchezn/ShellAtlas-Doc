---
title: "Persistence Communication Validation"
type: "review"
status: "open"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - review
  - testing
  - persistence
---

# Persistence Communication Validation

## Findings

- Supabase transport is valid at the contract level: `SupabasePostgrestApi` tests pass and the document repository can save drafts through `document_drafts`.
- Remote `/v1` transport is valid at the contract level: `ApiDocumentRepository` can save drafts through the API path.
- The backend currently does not expose a visible Confluence sync implementation or a dedicated unit-testable adapter, so Confluence communication is still unvalidated at code level.

## Evidence

- `./gradlew :core:data:desktopTest --tests com.shelldocs.core.data.supabase.SupabasePostgrestApiTest --tests com.shelldocs.core.data.repository.SupabaseDocumentRepositoryTest --tests com.shelldocs.core.data.repository.ApiDocumentRepositoryTest`

## Open Questions

- Should the backend expose a dedicated Confluence adapter/module so this boundary can be unit-tested?
- Should the API client keep the `/attributes` write path if the backend does not implement it yet?
