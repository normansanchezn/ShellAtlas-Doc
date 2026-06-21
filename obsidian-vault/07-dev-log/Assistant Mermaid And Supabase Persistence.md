---
title: "Dev Log - Assistant Mermaid And Supabase Persistence"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-20
tags:
  - shelldoc
  - dev-log
  - assistant
  - documents
  - supabase
---

# Dev Log - Assistant Mermaid And Supabase Persistence

## What changed

- Expanded assistant answers so they return longer, more helpful sections instead of short summaries.
- Added context-aware Mermaid generation for process/flow questions.
- Rendered assistant Mermaid output directly in chat with responsive Compose layouts for flow, sequence and timeline views.
- Fixed document persistence fallback so Supabase-backed apps no longer silently save only in demo memory.
- Added a dedicated create-document screen instead of reusing the regular workspace.
- Improved button hit areas, text cursor visibility and Enter-to-submit behavior in assistant chat and login.
- Restored the last active assistant conversation for authenticated users so reopening the tab no longer resets the
  thread.
- Added a conversation-context resolver so follow-up KT questions stay grounded in the current chat unless the user
  explicitly changes topic.

## Files created

- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantRichContent.kt`
- `feature/documents/src/commonMain/kotlin/com/shelldocs/feature/documents/ui/NewDocumentEditorPanel.kt`
- `core/data/src/commonMain/kotlin/com/shelldocs/core/data/repository/SupabaseDocumentRepository.kt`
- `core/common/src/commonMain/kotlin/com/shelldocs/core/common/persistence/SessionPreferences.kt`
- `obsidian-vault/08-diagrams/Assistant Mermaid And Persistence Flow.md`
- `obsidian-vault/07-dev-log/Assistant Mermaid And Supabase Persistence.md`

## Files modified

- `core/data/.../GroundedAssistantEngine.kt`
- `core/data/.../AssistantMermaidBuilder.kt`
- `core/domain/.../AskAssistantUseCase.kt`
- `feature/assistant/.../ChatMessageBubble.kt`
- `feature/assistant/.../ChatInputBar.kt`
- `feature/assistant/.../AssistantViewModel.kt`
- `feature/auth/.../SignInScreen.kt`
- `core/designsystem/.../ShellPrimaryButton.kt`
- `core/designsystem/.../ShellGhostButton.kt`
- `core/designsystem/.../ShellTextField.kt`
- `feature/documents/.../DocumentsViewModel.kt`
- `feature/documents/.../DocumentsScreen.kt`
- `feature/documents/.../ExplorerTreePanel.kt`
- `composeApp/.../AppContainer.kt`
- `docs/project-tree.md`

## Decisions made

- Mermaid should be generated in the assistant engine but rendered as native responsive Compose UI in chat.
- Supabase should be a first-class document persistence path, not only auth/profile wiring.
- New document creation should be isolated from the normal reading workspace to reduce accidental edits and make intent clearer.
- Conversation state is persisted through session-scoped preferences so the assistant can restore the current thread
  after tab switches without leaking the implementation into the feature module.
- Follow-up KT queries should use the recent chat transcript as grounding context; explicit topic shifts should bypass
  that context and search the new question normally.

## Issues found

- The real persistence bug came from `AppContainer` falling back to `DemoDocumentRepository` when `SHELLDOC_API_BASE_URL` was absent, even if Supabase was configured.
- `core:data` desktop tests currently have pre-existing `ExperimentalTime` opt-in failures unrelated to this task, so only assistant/documents desktop tests were used for focused validation.

## Tests added

- Re-ran desktop compile for `composeApp`.
- Re-ran focused desktop tests for `AssistantViewModelTest` and `DocumentsViewModelTest`.
- Re-ran assistant/domain desktop compile and desktop tests after wiring conversation restore and context resolution.

## Next steps

- Add dedicated repository tests for `SupabaseDocumentRepository` with mocked PostgREST responses.
- Expand assistant diagram heuristics with state/ER variants if the product later needs them.
