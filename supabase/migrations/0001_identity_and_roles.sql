-- ShellDocs · Identity & role delegation
-- Roles are delegated through public.user_roles; the catalog lives in
-- public.roles (owner / business / develop / viewer).

create table if not exists public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    full_name text not null default '',
    team text not null default '',
    email text not null default '',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.roles (
    key text primary key,
    display_name text not null,
    description text not null default ''
);

insert into public.roles (key, display_name, description) values
    ('owner', 'Owner', 'Full control: content, members, integrations and analytics'),
    ('business', 'Business', 'Reads knowledge, asks the assistant and views analytics'),
    ('develop', 'Develop', 'Creates, edits and publishes technical documentation'),
    ('viewer', 'Viewer', 'Read-only fallback for unassigned users')
on conflict (key) do update set
    display_name = excluded.display_name,
    description = excluded.description;

create table if not exists public.user_roles (
    user_id uuid primary key references auth.users (id) on delete cascade,
    role_key text not null references public.roles (key),
    assigned_by uuid references auth.users (id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- New sign-ups get a profile and the viewer role automatically.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    insert into public.profiles (id, email, full_name)
    values (new.id, new.email, coalesce(new.raw_user_meta_data ->> 'full_name', ''));
    insert into public.user_roles (user_id, role_key)
    values (new.id, 'viewer');
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Helper used by RLS policies.
create or replace function public.role_of(uid uuid)
returns text
language sql
security definer set search_path = public
stable
as $$
    select coalesce((select role_key from public.user_roles where user_id = uid), 'viewer');
$$;

alter table public.profiles enable row level security;
alter table public.roles enable row level security;
alter table public.user_roles enable row level security;

-- Every authenticated member can see the directory.
create policy "profiles are readable by members"
    on public.profiles for select
    to authenticated
    using (true);

create policy "users update their own profile"
    on public.profiles for update
    to authenticated
    using (id = auth.uid())
    with check (id = auth.uid());

create policy "role catalog is readable by members"
    on public.roles for select
    to authenticated
    using (true);

create policy "role assignments are readable by members"
    on public.user_roles for select
    to authenticated
    using (true);

-- Only owners can delegate roles (insert, update or revoke).
create policy "owners insert role assignments"
    on public.user_roles for insert
    to authenticated
    with check (public.role_of(auth.uid()) = 'owner');

create policy "owners update role assignments"
    on public.user_roles for update
    to authenticated
    using (public.role_of(auth.uid()) = 'owner')
    with check (public.role_of(auth.uid()) = 'owner');

create policy "owners delete role assignments"
    on public.user_roles for delete
    to authenticated
    using (public.role_of(auth.uid()) = 'owner');
