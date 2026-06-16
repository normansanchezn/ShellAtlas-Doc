drop policy "assistant cache readable" on "public"."assistant_intelligence";

drop policy "assistant cache updatable by members" on "public"."assistant_intelligence";

drop policy "assistant cache writable by members" on "public"."assistant_intelligence";

drop policy "audit readable" on "public"."audit_logs";

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

drop policy "profiles are readable by members" on "public"."profiles";

drop policy "users update their own profile" on "public"."profiles";

drop policy "role catalog is readable by members" on "public"."roles";

drop policy "sync runs readable" on "public"."sync_runs";

drop policy "owners delete role assignments" on "public"."user_roles";

drop policy "owners insert role assignments" on "public"."user_roles";

drop policy "owners update role assignments" on "public"."user_roles";

drop policy "role assignments are readable by members" on "public"."user_roles";

revoke delete on table "public"."assistant_intelligence" from "anon";

revoke insert on table "public"."assistant_intelligence" from "anon";

revoke references on table "public"."assistant_intelligence" from "anon";

revoke select on table "public"."assistant_intelligence" from "anon";

revoke trigger on table "public"."assistant_intelligence" from "anon";

revoke truncate on table "public"."assistant_intelligence" from "anon";

revoke update on table "public"."assistant_intelligence" from "anon";

revoke delete on table "public"."assistant_intelligence" from "authenticated";

revoke insert on table "public"."assistant_intelligence" from "authenticated";

revoke references on table "public"."assistant_intelligence" from "authenticated";

revoke select on table "public"."assistant_intelligence" from "authenticated";

revoke trigger on table "public"."assistant_intelligence" from "authenticated";

revoke truncate on table "public"."assistant_intelligence" from "authenticated";

revoke update on table "public"."assistant_intelligence" from "authenticated";

revoke delete on table "public"."assistant_intelligence" from "service_role";

revoke insert on table "public"."assistant_intelligence" from "service_role";

revoke references on table "public"."assistant_intelligence" from "service_role";

revoke select on table "public"."assistant_intelligence" from "service_role";

revoke trigger on table "public"."assistant_intelligence" from "service_role";

revoke truncate on table "public"."assistant_intelligence" from "service_role";

revoke update on table "public"."assistant_intelligence" from "service_role";

revoke delete on table "public"."profiles" from "anon";

revoke insert on table "public"."profiles" from "anon";

revoke references on table "public"."profiles" from "anon";

revoke select on table "public"."profiles" from "anon";

revoke trigger on table "public"."profiles" from "anon";

revoke truncate on table "public"."profiles" from "anon";

revoke update on table "public"."profiles" from "anon";

revoke delete on table "public"."profiles" from "authenticated";

revoke insert on table "public"."profiles" from "authenticated";

revoke references on table "public"."profiles" from "authenticated";

revoke select on table "public"."profiles" from "authenticated";

revoke trigger on table "public"."profiles" from "authenticated";

revoke truncate on table "public"."profiles" from "authenticated";

revoke update on table "public"."profiles" from "authenticated";

revoke delete on table "public"."profiles" from "service_role";

revoke insert on table "public"."profiles" from "service_role";

revoke references on table "public"."profiles" from "service_role";

revoke select on table "public"."profiles" from "service_role";

revoke trigger on table "public"."profiles" from "service_role";

revoke truncate on table "public"."profiles" from "service_role";

revoke update on table "public"."profiles" from "service_role";

revoke delete on table "public"."roles" from "anon";

revoke insert on table "public"."roles" from "anon";

revoke references on table "public"."roles" from "anon";

revoke select on table "public"."roles" from "anon";

revoke trigger on table "public"."roles" from "anon";

revoke truncate on table "public"."roles" from "anon";

revoke update on table "public"."roles" from "anon";

revoke delete on table "public"."roles" from "authenticated";

revoke insert on table "public"."roles" from "authenticated";

revoke references on table "public"."roles" from "authenticated";

revoke select on table "public"."roles" from "authenticated";

revoke trigger on table "public"."roles" from "authenticated";

revoke truncate on table "public"."roles" from "authenticated";

revoke update on table "public"."roles" from "authenticated";

revoke delete on table "public"."roles" from "service_role";

revoke insert on table "public"."roles" from "service_role";

revoke references on table "public"."roles" from "service_role";

revoke select on table "public"."roles" from "service_role";

revoke trigger on table "public"."roles" from "service_role";

revoke truncate on table "public"."roles" from "service_role";

revoke update on table "public"."roles" from "service_role";

revoke delete on table "public"."user_roles" from "anon";

revoke insert on table "public"."user_roles" from "anon";

revoke references on table "public"."user_roles" from "anon";

revoke select on table "public"."user_roles" from "anon";

revoke trigger on table "public"."user_roles" from "anon";

revoke truncate on table "public"."user_roles" from "anon";

revoke update on table "public"."user_roles" from "anon";

revoke delete on table "public"."user_roles" from "authenticated";

revoke insert on table "public"."user_roles" from "authenticated";

revoke references on table "public"."user_roles" from "authenticated";

revoke select on table "public"."user_roles" from "authenticated";

revoke trigger on table "public"."user_roles" from "authenticated";

revoke truncate on table "public"."user_roles" from "authenticated";

revoke update on table "public"."user_roles" from "authenticated";

revoke delete on table "public"."user_roles" from "service_role";

revoke insert on table "public"."user_roles" from "service_role";

revoke references on table "public"."user_roles" from "service_role";

revoke select on table "public"."user_roles" from "service_role";

revoke trigger on table "public"."user_roles" from "service_role";

revoke truncate on table "public"."user_roles" from "service_role";

revoke update on table "public"."user_roles" from "service_role";

alter table "public"."assistant_intelligence" drop constraint "assistant_intelligence_question_hash_key";

alter table "public"."audit_logs" drop constraint "audit_logs_actor_id_fkey";

alter table "public"."document_drafts" drop constraint "document_drafts_user_id_fkey";

alter table "public"."document_versions" drop constraint "document_versions_created_by_fkey";

alter table "public"."documents" drop constraint "documents_created_by_fkey";

alter table "public"."documents" drop constraint "documents_updated_by_fkey";

alter table "public"."profiles" drop constraint "profiles_id_fkey";

alter table "public"."user_roles" drop constraint "user_roles_assigned_by_fkey";

alter table "public"."user_roles" drop constraint "user_roles_role_key_fkey";

alter table "public"."user_roles" drop constraint "user_roles_user_id_fkey";

drop function if exists "public"."handle_new_user"();

drop function if exists "public"."role_of"(uid uuid);

alter table "public"."assistant_intelligence" drop constraint "assistant_intelligence_pkey";

alter table "public"."profiles" drop constraint "profiles_pkey";

alter table "public"."roles" drop constraint "roles_pkey";

alter table "public"."user_roles" drop constraint "user_roles_pkey";

drop index if exists "public"."assistant_intelligence_keywords_idx";

drop index if exists "public"."assistant_intelligence_pkey";

drop index if exists "public"."assistant_intelligence_question_hash_key";

drop index if exists "public"."documents_active_slug_idx";

drop index if exists "public"."idx_ai_intel_hash";

drop index if exists "public"."idx_ai_intel_keywords";

drop index if exists "public"."profiles_pkey";

drop index if exists "public"."roles_pkey";

drop index if exists "public"."user_roles_pkey";

drop index if exists "public"."document_versions_hash_idx";

drop table "public"."assistant_intelligence";

drop table "public"."profiles";

drop table "public"."roles";

drop table "public"."user_roles";

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

drop trigger if exists "on_auth_user_created" on "auth"."users";


