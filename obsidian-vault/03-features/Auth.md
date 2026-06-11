---
title: "Auth"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - auth
---

# Auth

## Summary

Authentication and role management entry flow for ShellDoc.

## Related Files

- `core/domain/entity/auth`
- `core/domain/usecase/auth`
- `core/data/demo/DemoAuthRepository.kt`
- `feature/auth`

## Development Notes

- Current behavior remains safe demo auth by default.
- Future Supabase-backed auth stays behind the same repository contracts.
- Desktop runtime can now read `SHELLDOC_SUPABASE_URL` and `SHELLDOC_SUPABASE_ANON_KEY` from a root `.env` file.
- The sign-in screen now explains demo-mode credentials when live config is absent.
