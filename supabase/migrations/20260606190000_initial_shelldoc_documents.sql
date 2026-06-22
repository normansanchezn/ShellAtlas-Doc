create extension if not exists pgcrypto;
create table if not exists documents (
  id uuid primary key default gen_random_uuid(),
  source_type text not null default 'manual',
  source_external_id text,
  source_space_key text,
  title text not null,
  slug text not null,
  status text not null default 'draft',
  classification text not null default 'internal',
  current_version_id uuid,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  constraint documents_status_check check (
    status in ('draft', 'published', 'updates_pending', 'conflicted', 'archived', 'deleted_source', 'locked', 'outdated')
  ),
  constraint documents_classification_check check (
    classification in ('public', 'internal', 'confidential', 'restricted')
  )
);
create unique index if not exists documents_slug_active_idx
  on documents(slug)
  where deleted_at is null;
create index if not exists documents_status_idx on documents(status) where deleted_at is null;
create index if not exists documents_source_idx on documents(source_type, source_external_id) where deleted_at is null;
create table if not exists document_versions (
  id uuid primary key default gen_random_uuid(),
  document_id uuid not null references documents(id) on delete cascade,
  version_number integer not null,
  title text not null,
  raw_markdown text not null,
  content_json jsonb not null,
  content_plaintext text not null,
  content_hash text not null,
  change_summary text,
  source_version text,
  created_by uuid,
  created_at timestamptz not null default now(),
  unique(document_id, version_number)
);
create unique index if not exists document_versions_hash_idx
  on document_versions(document_id, content_hash);
create index if not exists document_versions_document_idx
  on document_versions(document_id, version_number desc);
alter table documents
  add constraint documents_current_version_fk
  foreign key (current_version_id)
  references document_versions(id)
  deferrable initially deferred;
create table if not exists document_drafts (
  id uuid primary key default gen_random_uuid(),
  document_id uuid not null references documents(id) on delete cascade,
  user_id uuid not null,
  base_version_id uuid references document_versions(id),
  raw_markdown text not null,
  content_json jsonb not null,
  content_plaintext text not null,
  content_hash text not null,
  updated_at timestamptz not null default now(),
  unique(document_id, user_id)
);
create index if not exists document_drafts_updated_idx
  on document_drafts(updated_at);
create table if not exists document_attributes (
  id uuid primary key default gen_random_uuid(),
  document_id uuid not null references documents(id) on delete cascade,
  key text not null,
  value jsonb not null,
  source text not null default 'manual',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(document_id, key)
);
create index if not exists document_attributes_key_idx
  on document_attributes(key);
create table if not exists document_links (
  id uuid primary key default gen_random_uuid(),
  document_id uuid not null references documents(id) on delete cascade,
  link_type text not null,
  target_type text not null,
  target_id text not null,
  target_url text,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);
create index if not exists document_links_document_idx on document_links(document_id);
create index if not exists document_links_target_idx on document_links(target_type, target_id);
create table if not exists sync_runs (
  id uuid primary key default gen_random_uuid(),
  source_type text not null,
  status text not null,
  started_at timestamptz not null default now(),
  finished_at timestamptz,
  imported_count integer not null default 0,
  updated_count integer not null default 0,
  skipped_count integer not null default 0,
  failed_count integer not null default 0,
  metadata jsonb not null default '{}'::jsonb
);
create index if not exists sync_runs_source_started_idx
  on sync_runs(source_type, started_at desc);
create table if not exists audit_logs (
  id uuid primary key default gen_random_uuid(),
  actor_id uuid,
  action text not null,
  entity_type text not null,
  entity_id text not null,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);
create index if not exists audit_logs_entity_idx
  on audit_logs(entity_type, entity_id, created_at desc);
create index if not exists audit_logs_actor_idx
  on audit_logs(actor_id, created_at desc);
create or replace function set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;
drop trigger if exists documents_set_updated_at on documents;
create trigger documents_set_updated_at
before update on documents
for each row execute function set_updated_at();
drop trigger if exists document_attributes_set_updated_at on document_attributes;
create trigger document_attributes_set_updated_at
before update on document_attributes
for each row execute function set_updated_at();
alter table documents enable row level security;
alter table document_versions enable row level security;
alter table document_drafts enable row level security;
alter table document_attributes enable row level security;
alter table document_links enable row level security;
alter table sync_runs enable row level security;
alter table audit_logs enable row level security;
comment on table documents is 'Document identity and current published version pointer. Backend is the only writer for MVP.';
comment on table document_versions is 'Published immutable document versions. One row per publish, not per keystroke.';
comment on table document_drafts is 'Per-user autosave drafts. Drafts do not create published versions.';
comment on column document_versions.raw_markdown is 'Editable Markdown source of truth for the editor.';
comment on column document_versions.content_json is 'Normalized Markdown block JSON for render, diff, and future AI/search.';
comment on column document_versions.content_plaintext is 'Plain text extraction for low-cost search.';
