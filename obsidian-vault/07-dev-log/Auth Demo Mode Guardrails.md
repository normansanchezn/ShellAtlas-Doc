---
title: "Dev Log - Auth Demo Mode Guardrails"
type: "dev-log"
created: 2026-06-16
updated: 2026-06-16
tags:
  - shellatlas
  - dev-log
  - auth
---

# Dev Log - Auth Demo Mode Guardrails

## What changed

- Added a runtime guard that ignores `sb_secret_*` Supabase keys for client auth configuration.
- Kept the app in demo mode when a valid publishable anon key is not available.
- Updated auth test fixtures to use future session expiry timestamps.

## Files created

- `obsidian-vault/08-diagrams/Auth Runtime Selection Flow.md`
- `obsidian-vault/07-dev-log/Auth Demo Mode Guardrails.md`

## Files modified

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppEnvironment.kt`
- `composeApp/src/desktopMain/kotlin/com/shelldocs/app/DesktopAppConfig.kt`
- `composeApp/src/iosMain/kotlin/com/shelldocs/app/IosAppConfig.kt`
- `composeApp/src/androidMain/kotlin/com/shelldocs/app/AndroidAppConfig.kt`
- `composeApp/src/wasmJsMain/kotlin/com/shelldocs/app/WebAppConfig.kt`
- `composeApp/src/commonTest/kotlin/com/shelldocs/app/di/AppEnvironmentTest.kt`
- `core/domain/src/commonTest/kotlin/com/shelldocs/core/domain/fixtures/FakeAuthRepository.kt`
- `feature/auth/src/commonTest/kotlin/com/shelldocs/feature/auth/presentation/AuthViewModelTest.kt`
- `obsidian-vault/03-features/Auth.md`

## Decisions made

- Treat Supabase service-role secrets as invalid client auth keys.
- Prefer demo auth over a broken local live-auth configuration in development.

## Issues found

- Local auth could be pushed out of demo mode by an invalid client key.
- Auth fixtures used expiry dates close enough to current work that they were misleading during debugging.

## Tests added

- Added coverage for ignoring `sb_secret_*` in runtime auth configuration.

## Next steps

- If live local Supabase auth is needed, provide a real publishable anon key instead of a service-role secret.
