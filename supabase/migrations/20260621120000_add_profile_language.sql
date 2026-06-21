-- ShellDocs · UI display-language preference per user
alter table public.profiles
    add column if not exists language text not null default 'en'
    check (language in ('en', 'es', 'fr'));
