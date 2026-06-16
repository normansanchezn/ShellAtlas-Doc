---
title: "Dev Log - Supabase Local Bootstrap Recovery"
type: "dev-log"
created: 2026-06-16
updated: 2026-06-16
tags:
  - shellatlas
  - dev-log
  - supabase
  - docker
---

# Dev Log - Supabase Local Bootstrap Recovery

## What changed

- Reproduced `supabase start` locally until the exact failure surfaced.
- Confirmed Docker containers were created, but the stack was torn down because `seed.sql` referenced `public.assistant_intelligence` after a destructive remote snapshot migration had already dropped it.
- Replaced the destructive remote snapshot migration with a documented no-op to keep migration history without breaking bootstrap.
- Replaced the previous raw `pg_dump` seed with a small project-owned `public` seed that no longer depends on Supabase internal auth/storage schema versions.

## Files created

- `obsidian-vault/07-dev-log/Supabase Local Bootstrap Recovery.md`

## Files modified

- `supabase/migrations/20260612044949_remote_schema.sql`
- `supabase/seed.sql`
- `README.md`
- `obsidian-vault/07-dev-log/ShellAtlas Rename And Auth Bootstrap Fixes.md`

## Decisions made

- Keep the canonical bootstrap schema in the hand-authored migrations.
- Do not allow raw remote snapshot migrations to drop canonical local tables without review.

## Issues found

- `supabase start` was failing after migrations, not before Docker startup.
- The visible symptom looked like “containers do not start”, but the real cause was a destructive migration plus seed mismatch.
- The old `seed.sql` was a full internal dump containing `auth.*` rows and version-sensitive Supabase system data, which broke on the current local stack.

## Tests added

- No automated tests added. Validation is via `supabase start` / `supabase db pull`.

## Next steps

- Run `supabase start` and `supabase db pull` after this fix.
- If future remote pulls generate destructive diffs again, convert them into reviewed migrations instead of committing the raw snapshot.
