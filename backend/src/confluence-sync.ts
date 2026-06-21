import {createHash} from "node:crypto";
import type {SupabaseClient} from "@supabase/supabase-js";
import {parseMarkdown} from "./markdown-parser.ts";

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------

export interface ConfluenceConfig {
    baseUrl: string;
    /** Full `Authorization` header value — e.g. `Basic xxx` or `Bearer xxx`. */
    authHeader: string;
}

/**
 * Legacy path: Basic auth (email + API token) directly against the site
 * domain. Only works on sites that haven't disabled API-token Basic auth —
 * see `confluence-oauth.ts` for the OAuth 2.0 (3LO) path required on sites
 * that enforce `WWW-Authenticate: OAuth`.
 */
export function confluenceConfigFromEnv(): ConfluenceConfig | null {
    const baseUrl = process.env.CONFLUENCE_BASE_URL;
    const apiToken = process.env.CONFLUENCE_API_TOKEN;
    const userEmail = process.env.CONFLUENCE_USER_EMAIL;
    if (!baseUrl || !apiToken || !userEmail) return null;
    const credentials = Buffer.from(`${userEmail}:${apiToken}`).toString("base64");
    return {baseUrl: baseUrl.replace(/\/+$/, ""), authHeader: `Basic ${credentials}`};
}

// ---------------------------------------------------------------------------
// Confluence REST helpers
// ---------------------------------------------------------------------------

interface ConfluenceSpace {
    id: string;
    key: string;
    name: string;
    /** Confluence auto-parents any page created without an explicit parentId under this page — never null. */
    homepageId: string;
}

interface ConfluencePage {
    id: string;
    title: string;
    status: string;
    parentId: string | null;
    body?: {storage?: {value: string}};
    version?: {number: number; when: string};
    ancestors?: Array<{id: string; title: string}>;
}

// ---------------------------------------------------------------------------
// Confluence REST API v2 helpers
// (v1 `/rest/api/*` is gone on sites that enforce OAuth — see `confluence-oauth.ts`)
// ---------------------------------------------------------------------------

interface V2List<T> {
    results: T[];
    _links?: {next?: string};
}

async function confluenceFetch<T>(config: ConfluenceConfig, path: string): Promise<T> {
    const url = path.startsWith("http") ? path : `${config.baseUrl}${path}`;
    const response = await fetch(url, {
        headers: {
            Authorization: config.authHeader,
            Accept: "application/json",
        },
    });
    if (!response.ok) {
        const text = await response.text().catch(() => "");
        throw new Error(`Confluence API ${response.status}: ${text.slice(0, 200)}`);
    }
    return response.json() as Promise<T>;
}

async function confluenceFetchAllPages<T>(config: ConfluenceConfig, firstPath: string): Promise<T[]> {
    const items: T[] = [];
    let path: string | null = firstPath;
    while (path) {
        const batch = await confluenceFetch<V2List<T>>(config, path);
        items.push(...batch.results);
        path = batch._links?.next
            ? batch._links.next.startsWith("http")
                ? batch._links.next
                : `${config.baseUrl}${batch._links.next}`
            : null;
    }
    return items;
}

async function fetchAllSpaces(config: ConfluenceConfig): Promise<ConfluenceSpace[]> {
    type RawSpace = { id: string; key: string; name: string; type: string; homepageId: string };
    const raw = await confluenceFetchAllPages<RawSpace>(config, "/wiki/api/v2/spaces?limit=25");
    return raw
        .filter((s) => s.type !== "personal")
        .map((s) => ({id: s.id, key: s.key, name: s.name, homepageId: s.homepageId}));
}

/** Resolves the ancestor chain (root → immediate parent) for every page in a space, using only data already fetched. */
function resolveAncestors(pages: Array<{ id: string; title: string; parentId: string | null }>): Map<string, Array<{
    id: string;
    title: string
}>> {
    const byId = new Map(pages.map((p) => [p.id, p]));
    const result = new Map<string, Array<{ id: string; title: string }>>();
    for (const page of pages) {
        const chain: Array<{ id: string; title: string }> = [];
        let parentId = page.parentId;
        const seen = new Set<string>();
        while (parentId && byId.has(parentId) && !seen.has(parentId)) {
            seen.add(parentId);
            const parent = byId.get(parentId)!;
            chain.unshift({id: parent.id, title: parent.title});
            parentId = parent.parentId;
        }
        result.set(page.id, chain);
    }
    return result;
}

async function confluenceMutate<T>(
    config: ConfluenceConfig,
    path: string,
    method: "POST" | "PUT",
    body: unknown,
): Promise<T> {
    const url = `${config.baseUrl}${path}`;
    const response = await fetch(url, {
        method,
        headers: {
            Authorization: config.authHeader,
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        const text = await response.text().catch(() => "");
        throw new Error(`Confluence API ${response.status}: ${text.slice(0, 300)}`);
    }
    return response.json() as Promise<T>;
}

interface RawPageSummary {
    id: string;
    title: string;
    parentId: string | null;
    version: { number: number };
}

/** Finds a direct child of `parentId` matching `title` exactly. */
async function findChildPage(
    config: ConfluenceConfig,
    spaceId: string,
    parentId: string,
    title: string,
): Promise<RawPageSummary | null> {
    const params = new URLSearchParams({
        "space-id": spaceId,
        title,
        status: "current",
        limit: "10",
    });
    const list = await confluenceFetch<V2List<RawPageSummary>>(config, `/wiki/api/v2/pages?${params.toString()}`);
    return list.results.find((p) => p.parentId === parentId) ?? null;
}

/** Finds an existing page with `title` directly under `parentId`, or creates it (empty body) if missing — used for the Area/Platform/Version container pages. */
async function findOrCreateContainerPage(
    config: ConfluenceConfig,
    spaceId: string,
    parentId: string,
    title: string,
): Promise<string> {
    const existing = await findChildPage(config, spaceId, parentId, title);
    if (existing) return existing.id;

    const created = await confluenceMutate<{ id: string }>(config, "/wiki/api/v2/pages", "POST", {
        spaceId,
        status: "current",
        title,
        parentId,
        body: {representation: "storage", value: `<p>${title}</p>`},
    });
    return created.id;
}

/** Creates the page if `existingPageId` is null, otherwise updates its content (bumping the version number Confluence requires). */
async function createOrUpdatePage(
    config: ConfluenceConfig,
    spaceId: string,
    parentId: string,
    title: string,
    storageHtml: string,
    existingPageId: string | null,
): Promise<string> {
    if (!existingPageId) {
        const created = await confluenceMutate<{ id: string }>(config, "/wiki/api/v2/pages", "POST", {
            spaceId,
            status: "current",
            title,
            parentId,
            body: {representation: "storage", value: storageHtml},
        });
        return created.id;
    }

    const current = await confluenceFetch<RawPageSummary>(config, `/wiki/api/v2/pages/${existingPageId}`);
    await confluenceMutate(config, `/wiki/api/v2/pages/${existingPageId}`, "PUT", {
        id: existingPageId,
        status: "current",
        title,
        spaceId,
        body: {representation: "storage", value: storageHtml},
        version: {number: current.version.number + 1},
    });
    return existingPageId;
}

/**
 * Converts our markdown (the same content the platform shows in the editor)
 * into Confluence storage format (XHTML). Mirrors [storageToMarkdown] in
 * reverse — covers headings, lists, bold/italic, links and code fences,
 * which is the same subset our own markdown editor produces.
 */
function markdownToStorage(markdown: string): string {
    const escapeHtml = (text: string) =>
        text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

    const inline = (text: string) =>
        escapeHtml(text)
            .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
            .replace(/\*(.+?)\*/g, "<em>$1</em>")
            .replace(/\[(.+?)]\((.+?)\)/g, '<a href="$2">$1</a>');

    const lines = markdown.split("\n");
    const html: string[] = [];
    let inList: "ul" | "ol" | null = null;
    let inCodeBlock = false;
    let codeBuffer: string[] = [];

    const closeList = () => {
        if (inList) {
            html.push(`</${inList}>`);
            inList = null;
        }
    };

    for (const rawLine of lines) {
        const line = rawLine.trimEnd();

        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                html.push(
                    `<ac:structured-macro ac:name="code"><ac:plain-text-body><![CDATA[${codeBuffer.join("\n")}]]></ac:plain-text-body></ac:structured-macro>`,
                );
                codeBuffer = [];
                inCodeBlock = false;
            } else {
                closeList();
                inCodeBlock = true;
            }
            continue;
        }
        if (inCodeBlock) {
            codeBuffer.push(rawLine);
            continue;
        }

        const heading = /^(#{1,6})\s+(.*)$/.exec(line);
        if (heading) {
            closeList();
            html.push(`<h${heading[1].length}>${inline(heading[2])}</h${heading[1].length}>`);
            continue;
        }

        const bullet = /^[-*]\s+(.*)$/.exec(line);
        const ordered = /^\d+\.\s+(.*)$/.exec(line);
        if (bullet || ordered) {
            const tag = bullet ? "ul" : "ol";
            if (inList !== tag) {
                closeList();
                html.push(`<${tag}>`);
                inList = tag;
            }
            html.push(`<li>${inline((bullet ?? ordered)![1])}</li>`);
            continue;
        }

        closeList();
        if (line.trim()) html.push(`<p>${inline(line.trim())}</p>`);
    }
    closeList();
    return html.join("\n");
}

async function fetchPagesInSpace(config: ConfluenceConfig, space: ConfluenceSpace): Promise<ConfluencePage[]> {
    type RawPage = {
        id: string;
        status: string;
        title: string;
        parentId: string | null;
        body?: { storage?: { value: string } };
        version?: { number: number; createdAt: string };
    };
    const raw = await confluenceFetchAllPages<RawPage>(
        config,
        `/wiki/api/v2/spaces/${space.id}/pages?limit=50&body-format=storage`,
    );
    const ancestors = resolveAncestors(raw);
    return raw.map((p) => ({
        id: p.id,
        title: p.title,
        status: p.status,
        parentId: p.parentId,
        body: p.body,
        version: p.version ? {number: p.version.number, when: p.version.createdAt} : undefined,
        ancestors: ancestors.get(p.id) ?? [],
    }));
}

// ---------------------------------------------------------------------------
// Page tree (lightweight — no body)
// ---------------------------------------------------------------------------

export interface PageTreeNode {
    id: string;
    title: string;
    spaceKey: string;
    ancestors: string[];
    webUrl: string | null;
}

export async function fetchPageTree(config: ConfluenceConfig): Promise<PageTreeNode[]> {
    const spaces = await fetchAllSpaces(config);
    const siteBaseUrl = (process.env.CONFLUENCE_BASE_URL ?? "").replace(/\/+$/, "");
    const tree: PageTreeNode[] = [];
    for (const space of spaces) {
        const pages = await fetchPagesInSpace(config, space);
        for (const page of pages) {
            tree.push({
                id: page.id,
                title: page.title,
                spaceKey: space.key,
                ancestors: (page.ancestors ?? []).map((a) => a.title),
                webUrl: siteBaseUrl ? `${siteBaseUrl}/spaces/${space.key}/pages/${page.id}` : null,
            });
        }
    }
    return tree;
}

// ---------------------------------------------------------------------------
// XHTML → Markdown converter (Confluence storage format)
// ---------------------------------------------------------------------------

function storageToMarkdown(html: string): string {
    let md = html;
    md = md.replace(/<h([1-6])[^>]*>([\s\S]*?)<\/h\1>/gi, (_, level, content) => {
        return "\n" + "#".repeat(Number(level)) + " " + stripTags(content).trim() + "\n";
    });
    md = md.replace(/<ac:structured-macro[^>]*ac:name="code"[^>]*>[\s\S]*?<ac:plain-text-body><!\[CDATA\[([\s\S]*?)\]\]><\/ac:plain-text-body>[\s\S]*?<\/ac:structured-macro>/gi,
        (_, code) => "\n```\n" + code.trim() + "\n```\n");
    md = md.replace(/<li[^>]*>([\s\S]*?)<\/li>/gi, (_, content) => "- " + stripTags(content).trim() + "\n");
    md = md.replace(/<\/?(?:ul|ol)[^>]*>/gi, "\n");
    md = md.replace(/<br\s*\/?>/gi, "\n");
    md = md.replace(/<p[^>]*>([\s\S]*?)<\/p>/gi, (_, content) => "\n" + stripTags(content).trim() + "\n");
    md = md.replace(/<strong[^>]*>([\s\S]*?)<\/strong>/gi, "**$1**");
    md = md.replace(/<em[^>]*>([\s\S]*?)<\/em>/gi, "*$1*");
    md = md.replace(/<a[^>]*href="([^"]*)"[^>]*>([\s\S]*?)<\/a>/gi, "[$2]($1)");
    md = md.replace(/<ac:structured-macro[\s\S]*?<\/ac:structured-macro>/gi, "");
    md = md.replace(/<ac:[\s\S]*?<\/ac:[^>]+>/gi, "");
    md = md.replace(/<ac:[^/]*\/>/gi, "");
    md = stripTags(md);
    md = decodeHtmlEntities(md);
    md = md.replace(/\n{3,}/g, "\n\n");
    return md.trim();
}

function stripTags(html: string): string {
    return html.replace(/<[^>]+>/g, "");
}

const NAMED_ENTITIES: Record<string, string> = {
    amp: "&", lt: "<", gt: ">", quot: "\"", apos: "'", nbsp: " ",
    aacute: "á", eacute: "é", iacute: "í", oacute: "ó", uacute: "ú",
    Aacute: "Á", Eacute: "É", Iacute: "Í", Oacute: "Ó", Uacute: "Ú",
    ntilde: "ñ", Ntilde: "Ñ", uuml: "ü", Uuml: "Ü",
    ccedil: "ç", Ccedil: "Ç", agrave: "à", egrave: "è",
    iexcl: "¡", iquest: "¿", hellip: "…", mdash: "—", ndash: "–",
    laquo: "«", raquo: "»", ldquo: "“", rdquo: "”",
    lsquo: "‘", rsquo: "’", trade: "™", copy: "©", reg: "®",
};

function decodeHtmlEntities(text: string): string {
    return text
        .replace(/&#x([0-9a-fA-F]+);/g, (_, hex) => String.fromCodePoint(parseInt(hex, 16)))
        .replace(/&#(\d+);/g, (_, dec) => String.fromCodePoint(parseInt(dec, 10)))
        .replace(/&([a-zA-Z]+);/g, (match, name) => NAMED_ENTITIES[name] ?? match);
}

// ---------------------------------------------------------------------------
// Sync engine
// ---------------------------------------------------------------------------

export interface SyncResult {
    spacesProcessed: number;
    imported: number;
    updated: number;
    skipped: number;
    failed: number;
    errors: string[];
}

const SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

function slugify(title: string): string {
    return title
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");
}

export async function syncConfluence(
    config: ConfluenceConfig,
    db: SupabaseClient,
): Promise<SyncResult> {
    const result: SyncResult = {
        spacesProcessed: 0,
        imported: 0,
        updated: 0,
        skipped: 0,
        failed: 0,
        errors: [],
    };

    const spaces = await fetchAllSpaces(config);
    result.spacesProcessed = spaces.length;

    for (const space of spaces) {
        let pages: ConfluencePage[];
        try {
            pages = await fetchPagesInSpace(config, space);
        } catch (err) {
            result.errors.push(`Failed to fetch space ${space.key}: ${(err as Error).message}`);
            result.failed++;
            continue;
        }

        for (const page of pages) {
            try {
                await syncPage(config, db, page, space, result);
            } catch (err) {
                result.errors.push(`Page "${page.title}" (${page.id}): ${(err as Error).message}`);
                result.failed++;
            }
        }
    }

    await logSyncRun(db, result);
    return result;
}

async function syncPage(
    _config: ConfluenceConfig,
    db: SupabaseClient,
    page: ConfluencePage,
    space: ConfluenceSpace,
    result: SyncResult,
): Promise<void> {
    const storageHtml = page.body?.storage?.value ?? "";
    if (!storageHtml.trim()) {
        result.skipped++;
        return;
    }

    const rawMarkdown = `# ${page.title}\n\n${storageToMarkdown(storageHtml)}`;
    const contentHash = `sha256:${createHash("sha256").update(rawMarkdown).digest("hex")}`;

    const {data: existing} = await db
        .from("documents")
        .select("id, current_version_id")
        .eq("source_type", "confluence")
        .eq("source_external_id", page.id)
        .is("deleted_at", null)
        .maybeSingle();

    if (existing) {
        const {data: currentVersion} = await db
            .from("document_versions")
            .select("content_hash")
            .eq("id", existing.current_version_id)
            .single();

        if (currentVersion?.content_hash === contentHash) {
            result.skipped++;
            return;
        }

        await createNewVersion(db, existing.id, page.title, rawMarkdown, contentHash, page.version?.number?.toString());
        result.updated++;
    } else {
        await createDocument(db, page, space, rawMarkdown, contentHash);
        result.imported++;
    }
}

async function createDocument(
    db: SupabaseClient,
    page: ConfluencePage,
    space: ConfluenceSpace,
    rawMarkdown: string,
    contentHash: string,
): Promise<void> {
    const parsed = parseMarkdown(rawMarkdown);
    const slug = `${slugify(page.title)}-${page.id}`;

    const {data: doc, error: docErr} = await db
        .from("documents")
        .insert({
            title: page.title,
            slug,
            status: "published",
            source_type: "confluence",
            source_external_id: page.id,
            source_space_key: space.key,
        })
        .select("id")
        .single();

    if (docErr || !doc) throw new Error(docErr?.message ?? "Failed to create document");

    const contentJson = {schema_version: 1, blocks: parsed.contentJson.blocks};
    const {data: version, error: verErr} = await db
        .from("document_versions")
        .insert({
            document_id: doc.id,
            version_number: 1,
            title: page.title,
            raw_markdown: rawMarkdown,
            content_json: contentJson,
            content_plaintext: parsed.plainText,
            content_hash: contentHash,
            change_summary: `Imported from Confluence (space: ${space.key})`,
            source_version: page.version?.number?.toString() ?? null,
        })
        .select("id")
        .single();

    if (verErr || !version) throw new Error(verErr?.message ?? "Failed to create version");

    await db
        .from("documents")
        .update({current_version_id: version.id, updated_at: new Date().toISOString()})
        .eq("id", doc.id);

    const ancestors = (page.ancestors ?? []).map((a) => a.title);
    const attributes = [
        {document_id: doc.id, key: "module", value: ancestors[ancestors.length - 1] ?? space.name, source: "confluence"},
        {document_id: doc.id, key: "domain", value: space.name, source: "confluence"},
        {document_id: doc.id, key: "platform", value: "Confluence", source: "confluence"},
        {document_id: doc.id, key: "summary", value: parsed.plainText.slice(0, 300), source: "confluence"},
    ];
    await db.from("document_attributes").upsert(attributes, {onConflict: "document_id,key"});
}

async function createNewVersion(
    db: SupabaseClient,
    documentId: string,
    title: string,
    rawMarkdown: string,
    contentHash: string,
    sourceVersion?: string,
): Promise<void> {
    const parsed = parseMarkdown(rawMarkdown);

    const {data: lastVersion} = await db
        .from("document_versions")
        .select("version_number, id")
        .eq("document_id", documentId)
        .order("version_number", {ascending: false})
        .limit(1)
        .single();

    const nextNumber = (lastVersion?.version_number ?? 0) + 1;
    const contentJson = {schema_version: 1, blocks: parsed.contentJson.blocks};

    const {data: version, error: verErr} = await db
        .from("document_versions")
        .insert({
            document_id: documentId,
            version_number: nextNumber,
            title,
            raw_markdown: rawMarkdown,
            content_json: contentJson,
            content_plaintext: parsed.plainText,
            content_hash: contentHash,
            change_summary: "Updated from Confluence sync",
            source_version: sourceVersion ?? null,
        })
        .select("id")
        .single();

    if (verErr || !version) throw new Error(verErr?.message ?? "Failed to create version");

    await db
        .from("documents")
        .update({current_version_id: version.id, updated_at: new Date().toISOString()})
        .eq("id", documentId);

    await db
        .from("document_attributes")
        .upsert(
            [{document_id: documentId, key: "summary", value: parsed.plainText.slice(0, 300), source: "confluence"}],
            {onConflict: "document_id,key"},
        );
}

// ---------------------------------------------------------------------------
// Push: Supabase document -> Confluence page (Area / Platform / Version tree)
// ---------------------------------------------------------------------------

export interface PushResult {
    pageId: string;
    webUrl: string | null;
}

/**
 * Pushes one already-published document to Confluence, filing it under
 * Area > Platform > Version container pages (creating any of the three that
 * don't exist yet). Reuses the page tracked in `document_attributes.confluence_page_id`
 * on subsequent pushes so re-publishing updates the same page instead of
 * creating duplicates.
 */
export async function pushDocumentToConfluence(
    config: ConfluenceConfig,
    db: SupabaseClient,
    documentId: string,
): Promise<PushResult> {
    const {data: doc, error: docErr} = await db
        .from("documents")
        .select("id, title, current_version_id")
        .eq("id", documentId)
        .single<{ id: string; title: string; current_version_id: string | null }>();
    if (docErr || !doc) throw new Error(docErr?.message ?? "Document not found");
    if (!doc.current_version_id) throw new Error("Document has no published version yet");

    const [{data: version, error: verErr}, {data: attrRows}] = await Promise.all([
        db
            .from("document_versions")
            .select("raw_markdown")
            .eq("id", doc.current_version_id)
            .single<{ raw_markdown: string }>(),
        db
            .from("document_attributes")
            .select("key, value")
            .eq("document_id", documentId)
            .returns<Array<{ key: string; value: unknown }>>(),
    ]);
    if (verErr || !version) throw new Error(verErr?.message ?? "Version not found");

    const attrs = new Map((attrRows ?? []).map((r) => [r.key, r.value]));
    const area = String(attrs.get("area") ?? "").trim() || "Unsorted";
    const platform = String(attrs.get("platform") ?? "").trim() || "Unsorted";
    const version_ = String(attrs.get("application_version") ?? "").trim() || "Unversioned";
    const existingPageId = (attrs.get("confluence_page_id") as string | null) ?? null;

    const spaces = await fetchAllSpaces(config);
    const space = spaces[0];
    if (!space) throw new Error("No Confluence space available to push to");

    const areaPageId = await findOrCreateContainerPage(config, space.id, space.homepageId, area);
    const platformPageId = await findOrCreateContainerPage(config, space.id, areaPageId, platform);
    const versionPageId = await findOrCreateContainerPage(config, space.id, platformPageId, version_);

    const storageHtml = markdownToStorage(version.raw_markdown);
    const pageId = await createOrUpdatePage(
        config,
        space.id,
        versionPageId,
        doc.title,
        storageHtml,
        existingPageId,
    );

    await db.from("document_attributes").upsert(
        [{document_id: documentId, key: "confluence_page_id", value: pageId, source: "confluence"}],
        {onConflict: "document_id,key"},
    );

    const siteBaseUrl = (process.env.CONFLUENCE_BASE_URL ?? "").replace(/\/+$/, "");
    return {
        pageId,
        webUrl: siteBaseUrl ? `${siteBaseUrl}/spaces/${space.key}/pages/${pageId}` : null,
    };
}

async function logSyncRun(db: SupabaseClient, result: SyncResult): Promise<void> {
    await db.from("sync_runs").insert({
        source_type: "confluence",
        status: result.failed > 0 && result.imported === 0 && result.updated === 0 ? "failed" : "completed",
        imported_count: result.imported,
        updated_count: result.updated,
        skipped_count: result.skipped,
        failed_count: result.failed,
        metadata: {
            spaces_processed: result.spacesProcessed,
            errors: result.errors.slice(0, 20),
        },
    });
}
