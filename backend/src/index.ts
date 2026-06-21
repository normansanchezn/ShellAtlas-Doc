import {serve} from "@hono/node-server";
import {Hono} from "hono";
import {cors} from "hono/cors";
import {createClient} from "@supabase/supabase-js";
import {parseMarkdown} from "./markdown-parser.ts";
import {type ConfluenceConfig, confluenceConfigFromEnv, fetchPageTree, syncConfluence} from "./confluence-sync.ts";
import {
    buildAuthorizationUrl,
    completeOAuthLogin,
    confluenceConfigFromOAuth,
    hasStoredOAuthSession
} from "./confluence-oauth.ts";

// ---------------------------------------------------------------------------
// Supabase client
// ---------------------------------------------------------------------------

const SUPABASE_URL = process.env.SUPABASE_URL ?? "";
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY ?? "";

if (!SUPABASE_URL || !SUPABASE_SERVICE_ROLE_KEY) {
    throw new Error("Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY in environment");
}

const db = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
    auth: {persistSession: false},
});

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

const ATTRIBUTE_KEYS = [
    "summary",
    "owner",
    "module",
    "team",
    "status",
    "tags",
    "parent_folder_id",
    "platform",
    "domain",
    "area",
    "application_version",
    "confluence_page_id",
] as const;

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

type AttributeKey = (typeof ATTRIBUTE_KEYS)[number];

interface ContentJson {
    schema_version: 1;
    blocks: unknown[];
}

interface DocumentRow {
    id: string;
    title: string;
    slug: string;
    status: string;
    current_version_id: string | null;
    created_at: string;
    updated_at: string;
}

interface VersionRow {
    id: string;
    document_id: string;
    version_number: number;
    title: string;
    raw_markdown: string;
    content_json: ContentJson | null;
    content_plaintext: string;
    content_hash: string;
    change_summary: string | null;
    source_version: string | null;
    created_at: string;
}

interface AttributeRow {
    document_id: string;
    key: string;
    value: unknown;
}

interface AttributesMap {
    summary?: string;
    owner?: string;
    module?: string;
    team?: string;
    status?: string;
    tags?: string[];
    parent_folder_id?: string;
    platform?: string;
    domain?: string;
    area?: string;
    application_version?: string;
    confluence_page_id?: string;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function slugify(title: string): string {
    return title
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");
}

/** Normalise content_json from DB so it always has schema_version (snake_case). */
function normaliseContentJson(raw: unknown): ContentJson {
    if (raw == null || typeof raw !== "object") {
        return {schema_version: 1, blocks: []};
    }
    const obj = raw as Record<string, unknown>;
    const blocks = Array.isArray(obj["blocks"]) ? obj["blocks"] : [];
    return {schema_version: 1, blocks};
}

/** Convert ParsedDocument.contentJson (camelCase) → DB/wire snake_case shape. */
function parsedToContentJson(parsed: ReturnType<typeof parseMarkdown>): ContentJson {
    return {
        schema_version: 1,
        blocks: parsed.contentJson.blocks,
    };
}

function aggregateAttributes(rows: AttributeRow[], documentId: string): AttributesMap {
    const map: AttributesMap = {};
    for (const row of rows) {
        if (row.document_id !== documentId) continue;
        const k = row.key as AttributeKey;
        if (k === "tags") {
            map.tags = Array.isArray(row.value) ? (row.value as string[]) : [];
        } else {
            (map as Record<string, unknown>)[k] = row.value as string;
        }
    }
    return map;
}

function buildDocumentResponse(
    doc: DocumentRow,
    version: VersionRow | null,
    attrs: AttributesMap
): Record<string, unknown> {
    return {
        id: doc.id,
        title: doc.title,
        summary: attrs.summary ?? "",
        status: doc.status,
        raw_markdown: version?.raw_markdown ?? "",
        content_json: version ? normaliseContentJson(version.content_json) : {schema_version: 1, blocks: []},
        content_plaintext: version?.content_plaintext ?? "",
        attributes: {
            owner: attrs.owner ?? "",
            module: attrs.module ?? "",
            team: attrs.team ?? "",
            status: attrs.status ?? "",
            tags: attrs.tags ?? [],
            parent_folder_id: attrs.parent_folder_id ?? "",
            platform: attrs.platform ?? "",
            domain: attrs.domain ?? "",
            area: attrs.area ?? "",
            application_version: attrs.application_version ?? "",
        },
        created_at: doc.created_at,
        updated_at: doc.updated_at,
    };
}

function buildVersionResponse(version: VersionRow): Record<string, unknown> {
    return {
        id: version.id,
        document_id: version.document_id,
        version_number: version.version_number,
        title: version.title,
        raw_markdown: version.raw_markdown,
        content_json: normaliseContentJson(version.content_json),
        content_plaintext: version.content_plaintext,
        content_hash: version.content_hash,
        change_summary: version.change_summary ?? "",
        source_version: version.source_version ?? null,
        created_at: version.created_at,
    };
}

async function getNextVersionNumber(documentId: string): Promise<number> {
    const {data} = await db
        .from("document_versions")
        .select("version_number")
        .eq("document_id", documentId)
        .order("version_number", {ascending: false})
        .limit(1);
    if (!data || data.length === 0) return 1;
    return (data[0].version_number as number) + 1;
}

/**
 * Batch-fetch documents with their current versions and attributes.
 * If ids provided, filter to those. Otherwise return all non-deleted.
 */
async function fetchDocuments(ids?: string[]): Promise<Record<string, unknown>[]> {
    let docQuery = db
        .from("documents")
        .select("*")
        .is("deleted_at", null);

    if (ids && ids.length > 0) {
        docQuery = docQuery.in("id", ids);
    }

    const {data: docs, error: docErr} = await docQuery.returns<DocumentRow[]>();
    if (docErr) throw new Error(docErr.message);
    if (!docs || docs.length === 0) return [];

    const docIds = docs.map((d) => d.id);
    const versionIds = docs
        .map((d) => d.current_version_id)
        .filter((v): v is string => v != null);

    const [versionsResult, attrsResult] = await Promise.all([
        versionIds.length > 0
            ? db
                .from("document_versions")
                .select("*")
                .in("id", versionIds)
                .returns<VersionRow[]>()
            : Promise.resolve({data: [] as VersionRow[], error: null}),
        db
            .from("document_attributes")
            .select("document_id, key, value")
            .in("document_id", docIds)
            .returns<AttributeRow[]>(),
    ]);

    if (versionsResult.error) throw new Error(versionsResult.error.message);
    if (attrsResult.error) throw new Error(attrsResult.error.message);

    const versionMap = new Map<string, VersionRow>(
        (versionsResult.data ?? []).map((v) => [v.id, v])
    );
    const attrRows = attrsResult.data ?? [];

    return docs.map((doc) => {
        const version = doc.current_version_id ? versionMap.get(doc.current_version_id) ?? null : null;
        const attrs = aggregateAttributes(attrRows, doc.id);
        return buildDocumentResponse(doc, version, attrs);
    });
}

async function upsertAttributes(
    documentId: string,
    attrs: Partial<AttributesMap>
): Promise<void> {
    const rows = Object.entries(attrs)
        .filter(([, v]) => v !== undefined)
        .map(([key, value]) => ({
            document_id: documentId,
            key,
            value,
            source: "api",
        }));

    if (rows.length === 0) return;

    const {error} = await db
        .from("document_attributes")
        .upsert(rows, {onConflict: "document_id,key"});

    if (error) throw new Error(error.message);
}

// ---------------------------------------------------------------------------
// App
// ---------------------------------------------------------------------------

const app = new Hono();

app.use("*", cors());

// Request log — every call, so behavior is visible while developing locally
// instead of only finding out something broke when the UI shows an error.
app.use("*", async (c, next) => {
    const start = Date.now();
    await next();
    const ms = Date.now() - start;
    console.log(`[ShellDoc] ${c.req.method} ${c.req.path} -> ${c.res.status} (${ms}ms)`);
});

// Global error handler
app.onError((err, c) => {
    console.error("[ShellDoc]", err);
    const status = (err as { status?: number }).status ?? 500;
    return c.json({error: err.message ?? "Internal server error"}, status as 500);
});

// ---------------------------------------------------------------------------
// GET /v1/documents
// ---------------------------------------------------------------------------

app.get("/v1/documents", async (c) => {
    const documents = await fetchDocuments();
    console.log(`[ShellDoc] GET /v1/documents -> ${documents.length} document(s)`);
    return c.json({documents});
});

// ---------------------------------------------------------------------------
// GET /v1/search?q=query
// ---------------------------------------------------------------------------

app.get("/v1/search", async (c) => {
    const q = (c.req.query("q") ?? "").trim();
    if (!q) return c.json({documents: []});

    const pattern = `%${q}%`;

    // Search across documents title, version content_plaintext, and attribute values
    const [docTitleResult, versionResult, attrResult] = await Promise.all([
        db
            .from("documents")
            .select("id")
            .is("deleted_at", null)
            .ilike("title", pattern)
            .returns<{ id: string }[]>(),
        db
            .from("document_versions")
            .select("document_id")
            .ilike("content_plaintext", pattern)
            .returns<{ document_id: string }[]>(),
        db
            .from("document_attributes")
            .select("document_id")
            .ilike("value->>0", pattern) // works for text jsonb; cast for plain text search
            .returns<{ document_id: string }[]>(),
    ]);

    const matchedIds = new Set<string>([
        ...(docTitleResult.data ?? []).map((r) => r.id),
        ...(versionResult.data ?? []).map((r) => r.document_id),
        ...(attrResult.data ?? []).map((r) => r.document_id),
    ]);

    // Also search attribute text values via a broader query
    const {data: attrTextRows} = await db
        .from("document_attributes")
        .select("document_id, value")
        .returns<{ document_id: string; value: unknown }[]>();

    for (const row of attrTextRows ?? []) {
        const valStr = typeof row.value === "string"
            ? row.value
            : JSON.stringify(row.value ?? "");
        if (valStr.toLowerCase().includes(q.toLowerCase())) {
            matchedIds.add(row.document_id);
        }
    }

    if (matchedIds.size === 0) return c.json({documents: []});

    const documents = await fetchDocuments([...matchedIds]);
    return c.json({documents});
});

// ---------------------------------------------------------------------------
// GET /v1/documents/:id
// ---------------------------------------------------------------------------

app.get("/v1/documents/:id", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error} = await db
        .from("documents")
        .select("*")
        .eq("id", id)
        .is("deleted_at", null)
        .single<DocumentRow>();

    if (error || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const [versionsResult, attrsResult] = await Promise.all([
        doc.current_version_id
            ? db
                .from("document_versions")
                .select("*")
                .eq("id", doc.current_version_id)
                .single<VersionRow>()
            : Promise.resolve({data: null, error: null}),
        db
            .from("document_attributes")
            .select("document_id, key, value")
            .eq("document_id", id)
            .returns<AttributeRow[]>(),
    ]);

    const version = versionsResult.data ?? null;
    const attrs = aggregateAttributes(attrsResult.data ?? [], id);

    return c.json(buildDocumentResponse(doc, version, attrs));
});

// ---------------------------------------------------------------------------
// POST /v1/documents  (create)
// ---------------------------------------------------------------------------

app.post("/v1/documents", async (c) => {
    const body = await c.req.json<{
        title: string;
        summary?: string;
        raw_markdown: string;
        status?: string;
        attributes?: Partial<AttributesMap>;
        change_summary?: string;
    }>();

    if (!body.title || !body.raw_markdown) {
        return c.json({error: "title and raw_markdown are required"}, 400);
    }

    const parsed = parseMarkdown(body.raw_markdown);
    const slug = `${slugify(body.title)}-${Date.now()}`;
    const status = body.status ?? "draft";

    // Create document row first (no current_version_id yet)
    const {data: doc, error: docErr} = await db
        .from("documents")
        .insert({title: body.title, slug, status})
        .select()
        .single<DocumentRow>();

    if (docErr || !doc) {
        return c.json({error: docErr?.message ?? "Failed to create document"}, 500);
    }

    // Create first version
    const contentJson = parsedToContentJson(parsed);
    const {data: version, error: verErr} = await db
        .from("document_versions")
        .insert({
            document_id: doc.id,
            version_number: 1,
            title: body.title,
            raw_markdown: body.raw_markdown,
            content_json: contentJson,
            content_plaintext: parsed.plainText,
            content_hash: parsed.contentHash,
            change_summary: body.change_summary ?? "Initial version",
            source_version: null,
        })
        .select()
        .single<VersionRow>();

    if (verErr || !version) {
        return c.json({error: verErr?.message ?? "Failed to create version"}, 500);
    }

    // Update document.current_version_id
    const {error: updateErr} = await db
        .from("documents")
        .update({current_version_id: version.id, updated_at: new Date().toISOString()})
        .eq("id", doc.id);

    if (updateErr) {
        return c.json({error: updateErr.message}, 500);
    }

    // Upsert attributes (include summary)
    const attrs: Partial<AttributesMap> = {
        ...(body.attributes ?? {}),
        summary: body.summary ?? body.attributes?.summary ?? "",
    };
    await upsertAttributes(doc.id, attrs);

    // Re-fetch complete document
    const [docs] = await fetchDocuments([doc.id]);
    return c.json(docs, 201);
});

// ---------------------------------------------------------------------------
// POST /v1/documents/:id/publish  (update / publish)
// ---------------------------------------------------------------------------

app.post("/v1/documents/:id/publish", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error: fetchErr} = await db
        .from("documents")
        .select("*")
        .eq("id", id)
        .is("deleted_at", null)
        .single<DocumentRow>();

    if (fetchErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const body = await c.req.json<{
        title?: string;
        summary?: string;
        raw_markdown?: string;
        status?: string;
        attributes?: Partial<AttributesMap>;
        change_summary?: string;
    }>();

    const rawMarkdown = body.raw_markdown ?? "";
    const title = body.title ?? doc.title;
    const status = body.status ?? "published";

    let version: VersionRow | null = null;

    if (rawMarkdown) {
        const parsed = parseMarkdown(rawMarkdown);
        const versionNumber = await getNextVersionNumber(id);
        const contentJson = parsedToContentJson(parsed);

        const {data: ver, error: verErr} = await db
            .from("document_versions")
            .insert({
                document_id: id,
                version_number: versionNumber,
                title,
                raw_markdown: rawMarkdown,
                content_json: contentJson,
                content_plaintext: parsed.plainText,
                content_hash: parsed.contentHash,
                change_summary: body.change_summary ?? "",
                source_version: doc.current_version_id,
            })
            .select()
            .single<VersionRow>();

        if (verErr || !ver) {
            return c.json({error: verErr?.message ?? "Failed to create version"}, 500);
        }
        version = ver;
    }

    const updatePayload: Partial<DocumentRow & { updated_at: string }> = {
        title,
        status,
        updated_at: new Date().toISOString(),
    };
    if (version) updatePayload.current_version_id = version.id;

    const {error: updateErr} = await db
        .from("documents")
        .update(updatePayload)
        .eq("id", id);

    if (updateErr) {
        return c.json({error: updateErr.message}, 500);
    }

    if (body.attributes || body.summary !== undefined) {
        const attrs: Partial<AttributesMap> = {
            ...(body.attributes ?? {}),
        };
        if (body.summary !== undefined) attrs.summary = body.summary;
        await upsertAttributes(id, attrs);
    }

    const [result] = await fetchDocuments([id]);
    return c.json(result);
});

// ---------------------------------------------------------------------------
// POST /v1/documents/:id/draft
// ---------------------------------------------------------------------------

app.post("/v1/documents/:id/draft", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error: fetchErr} = await db
        .from("documents")
        .select("id, current_version_id")
        .eq("id", id)
        .is("deleted_at", null)
        .single<DocumentRow>();

    if (fetchErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const body = await c.req.json<{ raw_markdown: string }>();
    if (!body.raw_markdown) {
        return c.json({error: "raw_markdown is required"}, 400);
    }

    const parsed = parseMarkdown(body.raw_markdown);
    const contentJson = parsedToContentJson(parsed);

    const {error: upsertErr} = await db
        .from("document_drafts")
        .upsert(
            {
                document_id: id,
                user_id: SYSTEM_USER_ID,
                base_version_id: doc.current_version_id,
                raw_markdown: body.raw_markdown,
                content_json: contentJson,
                content_plaintext: parsed.plainText,
                content_hash: parsed.contentHash,
                updated_at: new Date().toISOString(),
            },
            {onConflict: "document_id,user_id"}
        );

    if (upsertErr) {
        return c.json({error: upsertErr.message}, 500);
    }

    return c.json({document_id: id, draft_saved: true});
});

// ---------------------------------------------------------------------------
// POST /v1/documents/:id/attributes
// ---------------------------------------------------------------------------

app.post("/v1/documents/:id/attributes", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error: docErr} = await db
        .from("documents")
        .select("id")
        .eq("id", id)
        .is("deleted_at", null)
        .single<{ id: string }>();

    if (docErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const body = await c.req.json<Partial<AttributesMap>>();
    await upsertAttributes(id, body);

    const [result] = await fetchDocuments([id]);
    return c.json(result);
});

// ---------------------------------------------------------------------------
// GET /v1/documents/:id/versions
// ---------------------------------------------------------------------------

app.get("/v1/documents/:id/versions", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error: docErr} = await db
        .from("documents")
        .select("id")
        .eq("id", id)
        .is("deleted_at", null)
        .single<{ id: string }>();

    if (docErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const {data, error} = await db
        .from("document_versions")
        .select("*")
        .eq("document_id", id)
        .order("version_number", {ascending: false})
        .returns<VersionRow[]>();

    if (error) {
        return c.json({error: error.message}, 500);
    }

    const versions = (data ?? []).map(buildVersionResponse);
    return c.json({versions});
});

// ---------------------------------------------------------------------------
// POST /v1/documents/:id/restore/:versionId
// ---------------------------------------------------------------------------

app.post("/v1/documents/:id/restore/:versionId", async (c) => {
    const id = c.req.param("id");
    const versionId = c.req.param("versionId");

    const {data: doc, error: docErr} = await db
        .from("documents")
        .select("*")
        .eq("id", id)
        .is("deleted_at", null)
        .single<DocumentRow>();

    if (docErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const {data: sourceVersion, error: verErr} = await db
        .from("document_versions")
        .select("*")
        .eq("id", versionId)
        .eq("document_id", id)
        .single<VersionRow>();

    if (verErr || !sourceVersion) {
        return c.json({error: "Version not found"}, 404);
    }

    const nextNumber = await getNextVersionNumber(id);
    // Append restore suffix to guarantee unique content_hash
    const restoredHash = `${sourceVersion.content_hash}-r-${Date.now()}`;

    const {data: newVersion, error: newVerErr} = await db
        .from("document_versions")
        .insert({
            document_id: id,
            version_number: nextNumber,
            title: sourceVersion.title,
            raw_markdown: sourceVersion.raw_markdown,
            content_json: sourceVersion.content_json,
            content_plaintext: sourceVersion.content_plaintext,
            content_hash: restoredHash,
            change_summary: `Restored from version ${sourceVersion.version_number}`,
            source_version: versionId,
        })
        .select()
        .single<VersionRow>();

    if (newVerErr || !newVersion) {
        return c.json({error: newVerErr?.message ?? "Failed to create restore version"}, 500);
    }

    const {error: updateErr} = await db
        .from("documents")
        .update({
            current_version_id: newVersion.id,
            title: sourceVersion.title,
            status: "published",
            updated_at: new Date().toISOString(),
        })
        .eq("id", id);

    if (updateErr) {
        return c.json({error: updateErr.message}, 500);
    }

    const [result] = await fetchDocuments([id]);
    return c.json(result);
});

// ---------------------------------------------------------------------------
// DELETE /v1/documents/:id
// ---------------------------------------------------------------------------

app.delete("/v1/documents/:id", async (c) => {
    const id = c.req.param("id");

    const {data: doc, error: fetchErr} = await db
        .from("documents")
        .select("id")
        .eq("id", id)
        .is("deleted_at", null)
        .single<{ id: string }>();

    if (fetchErr || !doc) {
        return c.json({error: "Document not found"}, 404);
    }

    const {error} = await db
        .from("documents")
        .update({
            deleted_at: new Date().toISOString(),
            status: "deleted_source",
            updated_at: new Date().toISOString(),
        })
        .eq("id", id);

    if (error) {
        return c.json({error: error.message}, 500);
    }

    return new Response(null, {status: 204});
});

// ---------------------------------------------------------------------------
// GET /v1/assistant/intelligence  — look up similar past answer
// ---------------------------------------------------------------------------

interface IntelligenceRow {
    id: string;
    question_hash: string;
    keywords: string[];
    cached_answer: string;
    diagram_type: string | null;
    source_doc_ids: string[];
    source_titles: string[];
    hit_count: number;
    confidence: string;
    created_at: string;
    updated_at: string;
}

app.get("/v1/assistant/intelligence", async (c) => {
    const hash = (c.req.query("hash") ?? "").trim();
    const keywordsParam = (c.req.query("keywords") ?? "").trim();
    const keywords = keywordsParam ? keywordsParam.split(",").filter(Boolean) : [];

    // Exact hash match first
    if (hash) {
        const {data: exact} = await db
            .from("assistant_intelligence")
            .select("*")
            .eq("question_hash", hash)
            .single<IntelligenceRow>();

        if (exact) {
            await db
                .from("assistant_intelligence")
                .update({hit_count: exact.hit_count + 1, updated_at: new Date().toISOString()})
                .eq("id", exact.id);
            return c.json({hit: true, entry: exact});
        }
    }

    // Keyword overlap match (>=60% overlap)
    if (keywords.length > 0) {
        const {data: rows} = await db
            .from("assistant_intelligence")
            .select("*")
            .overlaps("keywords", keywords)
            .order("hit_count", {ascending: false})
            .limit(10)
            .returns<IntelligenceRow[]>();

        if (rows && rows.length > 0) {
            const best = rows.reduce((a, b) => {
                const aOverlap = (a.keywords as string[]).filter((k) => keywords.includes(k)).length;
                const bOverlap = (b.keywords as string[]).filter((k) => keywords.includes(k)).length;
                return aOverlap >= bOverlap ? a : b;
            });
            const overlap = (best.keywords as string[]).filter((k) => keywords.includes(k)).length;
            const ratio = overlap / Math.max(keywords.length, (best.keywords as string[]).length);
            if (ratio >= 0.6) {
                await db
                    .from("assistant_intelligence")
                    .update({hit_count: best.hit_count + 1, updated_at: new Date().toISOString()})
                    .eq("id", best.id);
                return c.json({hit: true, entry: best});
            }
        }
    }

    return c.json({hit: false, entry: null});
});

// ---------------------------------------------------------------------------
// POST /v1/assistant/intelligence  — save new answer to cache
// ---------------------------------------------------------------------------

app.post("/v1/assistant/intelligence", async (c) => {
    const body = await c.req.json<{
        question_hash: string;
        keywords: string[];
        cached_answer: string;
        diagram_type?: string | null;
        source_doc_ids: string[];
        source_titles: string[];
        confidence: string;
    }>();

    if (!body.question_hash || !body.cached_answer) {
        return c.json({error: "question_hash and cached_answer are required"}, 400);
    }

    const {error} = await db.from("assistant_intelligence").upsert(
        {
            question_hash: body.question_hash,
            keywords: body.keywords ?? [],
            cached_answer: body.cached_answer,
            diagram_type: body.diagram_type ?? null,
            source_doc_ids: body.source_doc_ids ?? [],
            source_titles: body.source_titles ?? [],
            confidence: body.confidence ?? "medium",
            updated_at: new Date().toISOString(),
        },
        {onConflict: "question_hash"}
    );

    if (error) return c.json({error: error.message}, 500);
    return c.json({saved: true});
});

// ---------------------------------------------------------------------------
// Confluence auth resolution — prefer OAuth (required on sites that disabled
// Basic auth with API tokens); fall back to the legacy Basic-auth path.
// ---------------------------------------------------------------------------

async function resolveConfluenceConfig(): Promise<ConfluenceConfig | null> {
    if (hasStoredOAuthSession()) {
        return confluenceConfigFromOAuth();
    }
    return confluenceConfigFromEnv();
}

// ---------------------------------------------------------------------------
// GET /v1/sources/confluence/oauth/start — redirects to Atlassian consent
// ---------------------------------------------------------------------------

app.get("/v1/sources/confluence/oauth/start", (c) => {
    const state = crypto.randomUUID();
    return c.redirect(buildAuthorizationUrl(state));
});

// ---------------------------------------------------------------------------
// GET /v1/sources/confluence/oauth/callback — exchanges code for tokens
// ---------------------------------------------------------------------------

app.get("/v1/sources/confluence/oauth/callback", async (c) => {
    const code = c.req.query("code");
    if (!code) return c.json({error: "Missing ?code= from Atlassian redirect"}, 400);
    await completeOAuthLogin(code);
    return c.json({authorized: true});
});

// ---------------------------------------------------------------------------
// POST /v1/sources/confluence/sync
// ---------------------------------------------------------------------------

app.post("/v1/sources/confluence/sync", async (c) => {
    const confluenceConfig = await resolveConfluenceConfig();
    if (!confluenceConfig) {
        return c.json({error: "Confluence is not configured — visit GET /v1/sources/confluence/oauth/start to authorize"}, 400);
    }
    const result = await syncConfluence(confluenceConfig, db);
    return c.json(result);
});

// ---------------------------------------------------------------------------
// GET /v1/sources/confluence/tree
// ---------------------------------------------------------------------------

app.get("/v1/sources/confluence/tree", async (c) => {
    const confluenceConfig = await resolveConfluenceConfig();
    if (!confluenceConfig) {
        return c.json({error: "Confluence is not configured — visit GET /v1/sources/confluence/oauth/start to authorize"}, 400);
    }
    const tree = await fetchPageTree(confluenceConfig);
    return c.json({pages: tree, total: tree.length});
});

// ---------------------------------------------------------------------------
// GET /v1/connections/status — real status for the "Connections" screen.
// Jira/Azure DevOps report "disabled" honestly — we haven't built those
// integrations yet, no point faking a green checkmark for them.
// ---------------------------------------------------------------------------

app.get("/v1/connections/status", async (c) => {
    const confluenceAuthorized = hasStoredOAuthSession() || confluenceConfigFromEnv() !== null;
    let confluenceHost: string | null = null;
    try {
        confluenceHost = new URL(process.env.CONFLUENCE_BASE_URL ?? "").hostname || null;
    } catch {
        confluenceHost = null;
    }

    const {error: dbError} = await db.from("documents").select("id", {count: "exact", head: true}).limit(1);

    return c.json({
        confluence: {
            status: confluenceAuthorized ? "connected" : "disconnected",
            host: confluenceHost,
        },
        jira: {status: "disabled", host: null},
        azureDevops: {status: "disabled", host: null},
        database: {
            status: dbError ? "error" : "connected",
            detail: dbError?.message ?? null,
        },
    });
});

// ---------------------------------------------------------------------------
// Start server (Node.js via @hono/node-server)
// ---------------------------------------------------------------------------

const port = parseInt(process.env.PORT ?? "8787");
serve({fetch: app.fetch, port}, (info) => {
    console.log(`ShellDoc backend running at http://localhost:${info.port}`);
});
