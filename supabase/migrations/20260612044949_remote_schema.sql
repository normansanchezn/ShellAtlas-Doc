-- Remote schema snapshot intentionally neutralized for local bootstrap safety.
--
-- Context:
-- This file was generated from a remote schema pull and included destructive
-- drops for `assistant_intelligence`, `profiles`, `roles` and `user_roles`.
-- In the local migration chain those objects are the canonical tables created
-- by:
--   - 0001_identity_and_roles.sql
--   - 0003_assistant_intelligence.sql
--
-- When this snapshot ran after the canonical migrations, `supabase start`
-- succeeded in creating containers but failed during seed loading because the
-- seed expected tables that the snapshot had just removed.
--
-- Keeping this migration as a no-op preserves migration ordering/history while
-- allowing `supabase start`, `db reset` and future `db pull` operations to
-- complete against the intended local schema.

select 1;
