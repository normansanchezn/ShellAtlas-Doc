-- ShellAtlas local seed
--
-- Keep this file limited to project-owned `public` data.
-- Do not seed Supabase-managed internal schemas such as:
--   - auth
--   - storage
--   - realtime
--   - supabase_functions
--
-- Those schemas evolve with the local stack version and should be owned by the
-- Supabase containers themselves, not by a repository pg_dump snapshot.

begin;

insert into public.assistant_intelligence (
    id,
    question_hash,
    keywords,
    cached_answer,
    diagram_type,
    source_doc_ids,
    source_titles,
    intent,
    confidence,
    hit_count,
    created_at,
    updated_at
) values (
    '58fcdd37-b4e3-4659-8dae-902acdd1b9c8',
    'abc123',
    array['eosb', 'build', 'release'],
    'EoSB1 is the End of Sprint Build 1 process.',
    'flowchart TD',
    array['59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5'],
    array['EoSB #1 - Shell'],
    'question',
    'high',
    2,
    '2026-06-06 22:47:42.903699+00',
    '2026-06-06 22:47:47.548+00'
)
on conflict (id) do nothing;

insert into public.documents (
    id,
    source_type,
    source_external_id,
    source_space_key,
    title,
    slug,
    status,
    classification,
    current_version_id,
    created_by,
    updated_by,
    created_at,
    updated_at,
    deleted_at
) values (
    '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
    'manual',
    null,
    null,
    'EoSB #1 - Shell',
    'eosb-1-shell-1780784441775',
    'published',
    'internal',
    null,
    null,
    null,
    '2026-06-06 22:20:41.790492+00',
    '2026-06-06 22:20:41.807613+00',
    null
)
on conflict (id) do nothing;

insert into public.document_versions (
    id,
    document_id,
    version_number,
    title,
    raw_markdown,
    content_json,
    content_plaintext,
    content_hash,
    change_summary,
    source_version,
    created_by,
    created_at
) values (
    '70db46d9-b8b3-4954-a572-21740803209c',
    '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
    1,
    'EoSB #1 - Shell',
    '# EoSB1 Process for America''s App - Android (2025)

## Concepts

### EoSB1
**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.

## Quick Checklist

- Pause merges
- Update develop
- Verify versionCodes
- Run updateconfig.py
- Validate Azure secrets',
    '{
      "schema_version": 1,
      "blocks": [
        { "type": "heading", "level": 1, "text": "EoSB1 Process for America''s App - Android (2025)" },
        { "type": "heading", "level": 2, "text": "Concepts" },
        { "type": "heading", "level": 3, "text": "EoSB1" },
        { "type": "paragraph", "text": "**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished." },
        { "type": "heading", "level": 2, "text": "Quick Checklist" },
        { "type": "list", "style": "unordered", "items": ["Pause merges", "Update develop", "Verify versionCodes", "Run updateconfig.py", "Validate Azure secrets"] }
      ]
    }'::jsonb,
    'EoSB1 Process for America''s App - Android (2025) Concepts EoSB1 End of Sprint Build 1 is the first build generated in an iteration. Quick Checklist Pause merges Update develop Verify versionCodes Run updateconfig.py Validate Azure secrets',
    '8fdd8d907165f7d7a0f9aa8f7b9a100ea0ee4fdaf2ed5fb426d67d95c9df27cd',
    'Initial imported seed',
    'seed-v1',
    null,
    '2026-06-06 22:20:41.807613+00'
)
on conflict (id) do nothing;

update public.documents
set current_version_id = '70db46d9-b8b3-4954-a572-21740803209c'
where id = '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5'
  and current_version_id is distinct from '70db46d9-b8b3-4954-a572-21740803209c';

insert into public.document_attributes (
    id,
    document_id,
    key,
    value,
    source,
    created_at,
    updated_at
) values
    (
        '64857bd3-419c-4960-b09e-29c8fa4b5757',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'team',
        '"Snake"',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    ),
    (
        'cb48e4c5-3e1a-4e19-9c3c-5b57352efa8e',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'status',
        '"published"',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    ),
    (
        '6ce05a26-fedf-4236-b93a-b2054fb932c4',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'tags',
        '["Android","Snake","Team","Shell","Documentation","High"]',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    ),
    (
        '58a821ce-0a17-4488-972a-3e47b2e9b189',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'module',
        '"Android"',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    ),
    (
        '384e7204-ec99-4073-834b-6fcc3801c142',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'owner',
        '"Norman Sanchez"',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    ),
    (
        'f98ea0f7-33c5-4625-aa67-d8e73cc76a8d',
        '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5',
        'summary',
        '"Process to complete an End of Sprint Build 1."',
        'api',
        '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'
    )
on conflict (id) do nothing;

commit;
