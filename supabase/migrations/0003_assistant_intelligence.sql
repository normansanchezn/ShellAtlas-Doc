-- ShellDocs · Assistant intelligence cache (grounded answers, reusable).

create table if not exists public.assistant_intelligence (
    id uuid primary key default gen_random_uuid(),
    question_hash text not null unique,
    keywords text[] not null default '{}',
    cached_answer text not null,
    diagram_type text,
    source_doc_ids text[] not null default '{}',
    source_titles text[] not null default '{}',
    intent text not null default 'question',
    confidence text not null default 'medium',
    hit_count integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists assistant_intelligence_keywords_idx
    on public.assistant_intelligence using gin (keywords);

alter table public.assistant_intelligence enable row level security;

create policy "assistant cache readable" on public.assistant_intelligence
    for select to authenticated using (true);

create policy "assistant cache writable by members" on public.assistant_intelligence
    for insert to authenticated
    with check (public.role_of(auth.uid()) in ('owner','develop','business'));

create policy "assistant cache updatable by members" on public.assistant_intelligence
    for update to authenticated
    using (public.role_of(auth.uid()) in ('owner','develop','business'))
    with check (public.role_of(auth.uid()) in ('owner','develop','business'));
