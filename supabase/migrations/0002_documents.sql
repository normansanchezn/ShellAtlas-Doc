-- ShellDocs · Documents, versions, drafts, attributes, links and audit —
-- ported from the original ShellEnterpriseDoc schema, with role-aware RLS.

create table if not exists public.documents (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    slug text not null,
    status text not null default 'draft'
        check (status in ('draft','published','updates_pending','conflicted','archived','deleted_source','locked','outdated')),
    classification text not null default 'internal'
        check (classification in ('public','internal','confidential','restricted')),
    source_type text,
    source_external_id text,
    source_space_key text,
    current_version_id uuid,
    created_by uuid references auth.users (id),
    updated_by uuid references auth.users (id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz
);

create unique index if not exists documents_active_slug_idx
    on public.documents (slug) where deleted_at is null;

create table if not exists public.document_versions (
    id uuid primary key default gen_random_uuid(),
    document_id uuid not null references public.documents (id) on delete cascade,
    version_number integer not null,
    title text not null,
    raw_markdown text not null default '',
    content_json jsonb not null default '{}'::jsonb,
    content_plaintext text not null default '',
    content_hash text not null default '',
    change_summary text not null default '',
    source_version text,
    created_by uuid references auth.users (id),
    created_at timestamptz not null default now(),
    unique (document_id, version_number)
);

create index if not exists document_versions_hash_idx on public.document_versions (content_hash);

create table if not exists public.document_drafts (
    id uuid primary key default gen_random_uuid(),
    document_id uuid not null references public.documents (id) on delete cascade,
    user_id uuid not null references auth.users (id) on delete cascade,
    base_version_id uuid references public.document_versions (id),
    raw_markdown text not null default '',
    content_json jsonb not null default '{}'::jsonb,
    content_plaintext text not null default '',
    content_hash text not null default '',
    updated_at timestamptz not null default now(),
    unique (document_id, user_id)
);

create table if not exists public.document_attributes (
    id uuid primary key default gen_random_uuid(),
    document_id uuid not null references public.documents (id) on delete cascade,
    key text not null,
    value jsonb not null default 'null'::jsonb,
    source text not null default 'user',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (document_id, key)
);

create table if not exists public.document_links (
    id uuid primary key default gen_random_uuid(),
    document_id uuid not null references public.documents (id) on delete cascade,
    link_type text not null,
    target_type text not null,
    target_id text,
    target_url text,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create table if not exists public.sync_runs (
    id uuid primary key default gen_random_uuid(),
    source_type text not null,
    status text not null default 'running',
    started_at timestamptz not null default now(),
    finished_at timestamptz,
    imported_count integer not null default 0,
    updated_count integer not null default 0,
    skipped_count integer not null default 0,
    failed_count integer not null default 0,
    metadata jsonb not null default '{}'::jsonb
);

create table if not exists public.audit_logs (
    id uuid primary key default gen_random_uuid(),
    actor_id uuid references auth.users (id),
    action text not null,
    entity_type text not null,
    entity_id text,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

alter table public.documents enable row level security;
alter table public.document_versions enable row level security;
alter table public.document_drafts enable row level security;
alter table public.document_attributes enable row level security;
alter table public.document_links enable row level security;
alter table public.sync_runs enable row level security;
alter table public.audit_logs enable row level security;

-- Reads: any authenticated member.
create policy "documents readable" on public.documents
    for select to authenticated using (deleted_at is null);
create policy "versions readable" on public.document_versions
    for select to authenticated using (true);
create policy "attributes readable" on public.document_attributes
    for select to authenticated using (true);
create policy "links readable" on public.document_links
    for select to authenticated using (true);
create policy "sync runs readable" on public.sync_runs
    for select to authenticated using (true);
create policy "audit readable" on public.audit_logs
    for select to authenticated using (true);

-- Writes: owner and develop roles only (business/viewer are read-only).
create policy "editors insert documents" on public.documents
    for insert to authenticated
    with check (public.role_of(auth.uid()) in ('owner','develop'));
create policy "editors update documents" on public.documents
    for update to authenticated
    using (public.role_of(auth.uid()) in ('owner','develop'))
    with check (public.role_of(auth.uid()) in ('owner','develop'));
create policy "owners delete documents" on public.documents
    for delete to authenticated
    using (public.role_of(auth.uid()) = 'owner');

create policy "editors insert versions" on public.document_versions
    for insert to authenticated
    with check (public.role_of(auth.uid()) in ('owner','develop'));

create policy "users manage their own drafts" on public.document_drafts
    for all to authenticated
    using (user_id = auth.uid())
    with check (user_id = auth.uid());

create policy "editors write attributes" on public.document_attributes
    for all to authenticated
    using (public.role_of(auth.uid()) in ('owner','develop'))
    with check (public.role_of(auth.uid()) in ('owner','develop'));

create policy "editors write links" on public.document_links
    for all to authenticated
    using (public.role_of(auth.uid()) in ('owner','develop'))
    with check (public.role_of(auth.uid()) in ('owner','develop'));
