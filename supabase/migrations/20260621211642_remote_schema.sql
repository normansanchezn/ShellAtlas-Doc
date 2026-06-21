drop index if exists "public"."idx_ai_intel_hash";

drop index if exists "public"."idx_ai_intel_keywords";

CREATE UNIQUE INDEX documents_active_slug_idx ON public.documents USING btree (slug) WHERE (deleted_at IS NULL);

grant delete on table "public"."assistant_intelligence" to "anon";

grant insert on table "public"."assistant_intelligence" to "anon";

grant select on table "public"."assistant_intelligence" to "anon";

grant update on table "public"."assistant_intelligence" to "anon";

grant delete on table "public"."assistant_intelligence" to "authenticated";

grant insert on table "public"."assistant_intelligence" to "authenticated";

grant select on table "public"."assistant_intelligence" to "authenticated";

grant update on table "public"."assistant_intelligence" to "authenticated";

grant delete on table "public"."assistant_intelligence" to "service_role";

grant insert on table "public"."assistant_intelligence" to "service_role";

grant select on table "public"."assistant_intelligence" to "service_role";

grant update on table "public"."assistant_intelligence" to "service_role";

grant delete on table "public"."profiles" to "anon";

grant insert on table "public"."profiles" to "anon";

grant select on table "public"."profiles" to "anon";

grant update on table "public"."profiles" to "anon";

grant delete on table "public"."profiles" to "authenticated";

grant insert on table "public"."profiles" to "authenticated";

grant select on table "public"."profiles" to "authenticated";

grant update on table "public"."profiles" to "authenticated";

grant delete on table "public"."profiles" to "service_role";

grant insert on table "public"."profiles" to "service_role";

grant select on table "public"."profiles" to "service_role";

grant update on table "public"."profiles" to "service_role";

grant delete on table "public"."roles" to "anon";

grant insert on table "public"."roles" to "anon";

grant select on table "public"."roles" to "anon";

grant update on table "public"."roles" to "anon";

grant delete on table "public"."roles" to "authenticated";

grant insert on table "public"."roles" to "authenticated";

grant select on table "public"."roles" to "authenticated";

grant update on table "public"."roles" to "authenticated";

grant delete on table "public"."roles" to "service_role";

grant insert on table "public"."roles" to "service_role";

grant select on table "public"."roles" to "service_role";

grant update on table "public"."roles" to "service_role";

grant delete on table "public"."user_roles" to "anon";

grant insert on table "public"."user_roles" to "anon";

grant select on table "public"."user_roles" to "anon";

grant update on table "public"."user_roles" to "anon";

grant delete on table "public"."user_roles" to "authenticated";

grant insert on table "public"."user_roles" to "authenticated";

grant select on table "public"."user_roles" to "authenticated";

grant update on table "public"."user_roles" to "authenticated";

grant delete on table "public"."user_roles" to "service_role";

grant insert on table "public"."user_roles" to "service_role";

grant select on table "public"."user_roles" to "service_role";

grant update on table "public"."user_roles" to "service_role";


create
policy "audit readable"
  on "public"."audit_logs"
  as permissive
  for
select
    to authenticated
    using (true);



create
policy "attributes readable"
  on "public"."document_attributes"
  as permissive
  for
select
    to authenticated
    using (true);



create
policy "editors write attributes"
  on "public"."document_attributes"
  as permissive
  for all
  to authenticated
using ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])))
with check ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])));



  create
policy "users manage their own drafts"
  on "public"."document_drafts"
  as permissive
  for all
  to authenticated
using ((user_id = auth.uid()))
with check ((user_id = auth.uid()));



  create
policy "editors write links"
  on "public"."document_links"
  as permissive
  for all
  to authenticated
using ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])))
with check ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])));



  create
policy "links readable"
  on "public"."document_links"
  as permissive
  for
select
    to authenticated
    using (true);



create
policy "editors insert versions"
  on "public"."document_versions"
  as permissive
  for insert
  to authenticated
with check ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])));



  create
policy "versions readable"
  on "public"."document_versions"
  as permissive
  for
select
    to authenticated
    using (true);



create
policy "documents readable"
  on "public"."documents"
  as permissive
  for
select
    to authenticated
    using ((deleted_at IS NULL));



create
policy "editors insert documents"
  on "public"."documents"
  as permissive
  for insert
  to authenticated
with check ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])));



  create
policy "editors update documents"
  on "public"."documents"
  as permissive
  for
update
    to authenticated
    using ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])))
with check ((public.role_of(auth.uid()) = ANY (ARRAY['owner'::text, 'develop'::text])));



create
policy "owners delete documents"
  on "public"."documents"
  as permissive
  for delete
to authenticated
using ((public.role_of(auth.uid()) = 'owner'::text));



  create
policy "sync runs readable"
  on "public"."sync_runs"
  as permissive
  for
select
    to authenticated
    using (true);



