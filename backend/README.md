# ShellDoc Backend API

This folder documents the first backend boundary for the SwiftUI app.

The app must call this API over HTTPS. It must not connect directly to Jira, Confluence, GitHub, or Supabase with privileged credentials.

Required server-side environment variables:

```env
SUPABASE_URL=
SUPABASE_SERVICE_ROLE_KEY=
DATABASE_URL=
ATLASSIAN_BASE_URL=
ATLASSIAN_CLIENT_ID=
ATLASSIAN_CLIENT_SECRET=
GITHUB_APP_ID=
GITHUB_PRIVATE_KEY=
ENCRYPTION_MASTER_KEY=
```

The SwiftUI app may be configured with:

```env
SHELLDOC_API_BASE_URL=https://api.example.com
SHELLDOC_API_BEARER_TOKEN=optional-user-session-token
```

Do not put `SUPABASE_SERVICE_ROLE_KEY`, Atlassian secrets, GitHub private keys, or encryption keys in the app.

## Initial API Contract

```http
GET    /v1/documents
GET    /v1/documents/{id}
POST   /v1/documents
POST   /v1/documents/{id}/draft
POST   /v1/documents/{id}/publish
GET    /v1/documents/{id}/versions
GET    /v1/documents/{id}/versions/{version}
POST   /v1/documents/{id}/restore/{version}
DELETE /v1/documents/{id}
GET    /v1/search?q={query}
POST   /v1/sync/confluence
```

## Document Response Shape

```json
{
  "id": "uuid",
  "title": "Payment Authorization Flow",
  "summary": "Short description",
  "status": "published",
  "raw_markdown": "# Payment Authorization Flow\n\n...",
  "content_json": {
    "schema_version": 1,
    "blocks": []
  },
  "content_plaintext": "Payment Authorization Flow ...",
  "attributes": {
    "owner": "Owner",
    "module": "Transactions",
    "team": "Payments",
    "status": "published",
    "tags": ["android", "payments"],
    "parent_folder_id": "android-transactions"
  },
  "created_at": "2026-06-06T18:00:00Z",
  "updated_at": "2026-06-06T18:00:00Z"
}
```

## Save Rules

- `POST /v1/documents/{id}/draft` upserts `document_drafts`.
- `POST /v1/documents/{id}/publish` parses Markdown, creates a `document_versions` row only when `content_hash` changed, updates `documents.current_version_id`, and writes `audit_logs`.
- `POST /v1/documents` creates the document identity and initial published version.
- Markdown parsing happens only on the backend.
- Supabase Storage is reserved for attachments or heavy snapshots, not normal document content.
