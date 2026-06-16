---
title: "Dev Log - ShellAtlas Rename And Auth Bootstrap Fixes"
type: "dev-log"
created: 2026-06-16
updated: 2026-06-16
tags:
  - shellatlas
  - dev-log
  - auth
  - supabase
---

# Dev Log - ShellAtlas Rename And Auth Bootstrap Fixes

## What changed

- Completed the visible product rename from ShellDoc/ShellDocs to ShellAtlas in primary app surfaces.
- Fixed the Supabase remote schema snapshot so it drops `on_auth_user_created` before `handle_new_user()`.
- Improved sign-in error feedback for local development failures.
- Hardened runtime config parsing to ignore placeholder values from copied example env files.
- Confirmed the password field exposes a reliable show/hide eye control.
- Neutralized the destructive `20260612044949_remote_schema.sql` snapshot so `supabase start` no longer drops canonical local tables before `seed.sql`.

## Files created

- `obsidian-vault/07-dev-log/ShellAtlas Rename And Auth Bootstrap Fixes.md`

## Files modified

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppEnvironment.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/presentation/AuthViewModel.kt`
- `feature/auth/src/commonMain/kotlin/com/shelldocs/feature/auth/ui/SignInScreen.kt`
- `feature/auth/src/commonTest/kotlin/com/shelldocs/feature/auth/presentation/AuthViewModelTest.kt`
- `core/designsystem/src/commonMain/kotlin/com/shelldocs/core/designsystem/atoms/ShellTextField.kt`
- `core/data/src/commonMain/kotlin/com/shelldocs/core/data/assistant/AssistantMermaidBuilder.kt`
- `supabase/migrations/20260612044949_remote_schema.sql`
- `.env.example`
- `composeApp/src/androidMain/AndroidManifest.xml`
- `composeApp/build.gradle.kts`
- `composeApp/src/wasmJsMain/resources/index.html`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/ui/WorkspaceRail.kt`
- `core/domain/src/commonMain/kotlin/com/shelldocs/core/domain/usecase/assistant/BuildWelcomeMessageUseCase.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/AssistantHeader.kt`
- `feature/assistant/src/commonMain/kotlin/com/shelldocs/feature/assistant/ui/ChatMessageBubble.kt`
- `README.md`
- `docs/project-tree.md`
- `obsidian-vault/03-features/Auth.md`

## Decisions made

- Keep package names and module names stable for now, but rename user-facing product strings to ShellAtlas.
- Ignore placeholder env values instead of attempting broken local auth requests.
- Repair the Supabase bootstrap order directly in the schema snapshot instead of hiding the migration failure.
- Treat hand-authored local migrations as the source of truth for bootstrap, and keep remote snapshot migrations non-destructive unless they are reviewed and rewritten.

## Issues found

- `.env.example` contained a sensitive-looking Confluence token and personal email, so both were replaced with placeholders.
- The local Supabase stack is currently not running, which matches the sign-in failure shown in the screenshot.
- The later remote snapshot migration dropped `assistant_intelligence`, `profiles`, `roles` and `user_roles`, which caused `supabase start` to stop containers after `seed.sql` hit `SQLSTATE 42P01`.

## Tests added

- Added auth ViewModel coverage for local sign-in network failures.

## Next steps

- Start Supabase again after the migration fix and verify local sign-in against the repaired stack.
- Continue renaming remaining secondary references if internal code-level branding also needs to change later.
- Verify `supabase db pull` again now that local bootstrap succeeds cleanly.
