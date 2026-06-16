---
title: "Auth"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellAtlas"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-16
tags:
  - shellatlas
  - auth
---

# Auth

## Summary

Authentication and role management entry flow for ShellAtlas.

## Related Files

- `core/domain/entity/auth`
- `core/domain/usecase/auth`
- `core/data/demo/DemoAuthRepository.kt`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppEnvironment.kt`
- `feature/auth`

## Development Notes

- Current behavior remains safe demo auth by default.
- Future Supabase-backed auth stays behind the same repository contracts.
- Desktop runtime can now read `SHELLDOC_DEV_SUPABASE_URL` and `SHELLDOC_DEV_SUPABASE_ANON_KEY` from a root `.env` file, with `SHELLDOC_APP_ENVIRONMENT=DEV` selecting the local profile.
- The sign-in screen now explains demo-mode credentials when live config is absent.
- Placeholder values in copied env files are ignored so local development can stay in demo mode until real config is ready.
- The password field now includes a show/hide control, and sign-in errors explain when the local Supabase stack is unavailable.
- Supabase client auth now ignores `sb_secret_*` keys so a backend service-role secret cannot accidentally disable demo mode in local development.
- Test auth sessions now use future expiry dates so auth fixtures do not look stale during debugging.

## Mermaid Diagram

- `obsidian-vault/08-diagrams/Auth Runtime Selection Flow.md`
