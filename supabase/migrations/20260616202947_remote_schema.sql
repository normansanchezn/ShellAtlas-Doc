drop policy "audit readable" on "public"."audit_logs";
-- NOTE: drops of "assistant_intelligence", "profiles", "roles" and
-- "user_roles" policies/grants/constraints/indexes/tables removed from this
-- auto-generated diff — they captured a remote state where those tables had
-- been wiped out, which broke every fresh `db reset` / first-time `db push`
-- (0001/0003 create them, this file then deleted them again before any later
-- migration or the seed script could touch them).

drop policy "attributes readable" on "public"."document_attributes";
drop policy "editors write attributes" on "public"."document_attributes";
drop policy "users manage their own drafts" on "public"."document_drafts";
drop policy "editors write links" on "public"."document_links";
drop policy "links readable" on "public"."document_links";
drop policy "editors insert versions" on "public"."document_versions";
drop policy "versions readable" on "public"."document_versions";
drop policy "documents readable" on "public"."documents";
drop policy "editors insert documents" on "public"."documents";
drop policy "editors update documents" on "public"."documents";
drop policy "owners delete documents" on "public"."documents";
drop policy "sync runs readable" on "public"."sync_runs";
alter table "public"."audit_logs" drop constraint "audit_logs_actor_id_fkey";
alter table "public"."document_drafts" drop constraint "document_drafts_user_id_fkey";
alter table "public"."document_versions" drop constraint "document_versions_created_by_fkey";
alter table "public"."documents" drop constraint "documents_created_by_fkey";
alter table "public"."documents" drop constraint "documents_updated_by_fkey";
drop index if exists "public"."documents_active_slug_idx";
drop index if exists "public"."document_versions_hash_idx";
alter table "public"."audit_logs" alter column "entity_id" set not null;
alter table "public"."document_attributes" alter column "source" set default 'manual'::text;
alter table "public"."document_attributes" alter column "value" drop default;
alter table "public"."document_drafts" alter column "content_hash" drop default;
alter table "public"."document_drafts" alter column "content_json" drop default;
alter table "public"."document_drafts" alter column "content_plaintext" drop default;
alter table "public"."document_drafts" alter column "raw_markdown" drop default;
alter table "public"."document_links" alter column "target_id" set not null;
alter table "public"."document_versions" alter column "change_summary" drop default;
alter table "public"."document_versions" alter column "change_summary" drop not null;
alter table "public"."document_versions" alter column "content_hash" drop default;
alter table "public"."document_versions" alter column "content_json" drop default;
alter table "public"."document_versions" alter column "content_plaintext" drop default;
alter table "public"."document_versions" alter column "raw_markdown" drop default;
alter table "public"."documents" alter column "source_type" set default 'manual'::text;
alter table "public"."documents" alter column "source_type" set not null;
alter table "public"."sync_runs" alter column "status" drop default;
CREATE UNIQUE INDEX document_versions_hash_idx ON public.document_versions USING btree (document_id, content_hash);
