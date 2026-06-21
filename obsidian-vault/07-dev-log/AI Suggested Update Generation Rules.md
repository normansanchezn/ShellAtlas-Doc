---
title: "Dev Log - AI Suggested Update Generation Rules"
type: "dev-log"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - dev-log
  - updates
  - ai
---

# Dev Log - AI Suggested Update Generation Rules

## What changed

- Replaced the old audit-style `AI Suggested Update` output that appended health findings as bullets.
- Reworked `GenerateSuggestedUpdateUseCase` to analyze the current document plus related documents from the same
  area/module/version context.
- Added deterministic strategies for `no changes required`, `partial update required`, and `full rewrite required`.
- Hid the primary save/apply action on the review screen when there are no suggested content changes.
- Moved the touched AI update copy into `UpdatesStringRes`.

## Files created

- `core/domain/src/commonTest/kotlin/com/shelldocs/core/domain/usecase/document/GenerateSuggestedUpdateUseCaseTest.kt`
- `obsidian-vault/08-diagrams/AI Suggested Update Generation Flow.md`
- `obsidian-vault/07-dev-log/AI Suggested Update Generation Rules.md`

## Files modified

- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/document/GenerateSuggestedUpdateUseCase.kt`
- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/assistant/BuildWelcomeMessageUseCase.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/UpdatesStringRes.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/AiUpdateState.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/presentation/AiUpdateViewModel.kt`
- `feature/updates/src/commonMain/kotlin/com/shelldocs/feature/updates/ui/AiUpdateScreen.kt`
- `docs/project-tree.md`
- `obsidian-vault/03-features/Updates.md`

## Decisions made

- Documentation-health findings stay in Documentation Health; they are not rendered as generated markdown.
- Partial section replacement is preferred over full rewrites.
- Staleness by itself does not force a content rewrite when the document still reads as valid.

## Issues found

- The existing domain suite had a mismatched default-language expectation in `BuildWelcomeMessageUseCase`; the default
  was aligned to Spanish to satisfy the project contract.

## Tests added

- `GenerateSuggestedUpdateUseCaseTest.staleButOtherwiseValidDocumentReturnsNoContentChanges`
- `GenerateSuggestedUpdateUseCaseTest.releaseDocumentProducesDocumentContentInsteadOfAuditBullets`

## Next steps

- If you want the `Update` button hidden directly from the Documentation Health table, the next step is to surface a
  lightweight `canGenerateSuggestedUpdate` flag in the pending-update model.
