import {existsSync, readFileSync, writeFileSync} from "node:fs";
import {fileURLToPath} from "node:url";
import type {ConfluenceConfig} from "./confluence-sync.ts";

/**
 * OAuth 2.0 (3LO) login for sites that disabled Basic auth with API tokens
 * (they answer with `WWW-Authenticate: OAuth` on the legacy endpoints).
 * Tokens are cached on disk for local dev — good enough for a single-user
 * backend; swap for a real token table if this ever serves more than one user.
 */

const TOKEN_STORE_PATH = fileURLToPath(new URL("../.oauth-tokens.json", import.meta.url));

interface StoredTokens {
    accessToken: string;
    refreshToken: string;
    expiresAt: number;
    cloudId: string;
}

// Granular scopes — required by the REST API v2 endpoints (`/wiki/api/v2/*`),
// the only ones this site accepts. The older "classic" scopes
// (`confluence-content.all` etc.) only work against the deprecated v1 API.
const SCOPES = [
    "read:page:confluence",
    "write:page:confluence",
    "read:content:confluence",
    "read:content-details:confluence",
    "read:content.metadata:confluence",
    "read:space:confluence",
    "read:space-details:confluence",
    "read:audit-log:confluence",
    "write:audit-log:confluence",
    "write:content:confluence",
    "offline_access",
].join(" ");

function requiredEnv(name: string): string {
    const value = process.env[name];
    if (!value) throw new Error(`Missing ${name} in environment`);
    return value;
}

export function buildAuthorizationUrl(state: string): string {
    const params = new URLSearchParams({
        audience: "api.atlassian.com",
        client_id: requiredEnv("CONFLUENCE_CLIENT_ID"),
        scope: SCOPES,
        redirect_uri: requiredEnv("CONFLUENCE_REDIRECT_URI"),
        state,
        response_type: "code",
        prompt: "consent",
    });
    return `https://auth.atlassian.com/authorize?${params.toString()}`;
}

interface TokenResponse {
    access_token: string;
    refresh_token: string;
    expires_in: number;
}

async function postTokenRequest(body: Record<string, string>): Promise<TokenResponse> {
    const response = await fetch("https://auth.atlassian.com/oauth/token", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`Atlassian token request failed: ${response.status} ${await response.text()}`);
    }
    return response.json() as Promise<TokenResponse>;
}

async function fetchAccessibleCloudId(accessToken: string): Promise<string> {
    const response = await fetch("https://api.atlassian.com/oauth/token/accessible-resources", {
        headers: {Authorization: `Bearer ${accessToken}`, Accept: "application/json"},
    });
    if (!response.ok) {
        throw new Error(`Failed to list accessible Atlassian sites: ${response.status} ${await response.text()}`);
    }
    const sites = (await response.json()) as Array<{ id: string; url: string }>;
    const siteHost = new URL(requiredEnv("CONFLUENCE_BASE_URL")).hostname;
    const site = sites.find((s) => new URL(s.url).hostname === siteHost) ?? sites[0];
    if (!site) throw new Error("No accessible Atlassian sites for this account/app");
    return site.id;
}

function loadStoredTokens(): StoredTokens | null {
    if (!existsSync(TOKEN_STORE_PATH)) return null;
    return JSON.parse(readFileSync(TOKEN_STORE_PATH, "utf-8")) as StoredTokens;
}

function saveStoredTokens(tokens: StoredTokens): void {
    writeFileSync(TOKEN_STORE_PATH, JSON.stringify(tokens, null, 2));
}

/** Exchanges the `code` from the OAuth redirect for tokens and caches them. */
export async function completeOAuthLogin(code: string): Promise<void> {
    const token = await postTokenRequest({
        grant_type: "authorization_code",
        client_id: requiredEnv("CONFLUENCE_CLIENT_ID"),
        client_secret: requiredEnv("CONFLUENCE_CLIENT_SECRET"),
        code,
        redirect_uri: requiredEnv("CONFLUENCE_REDIRECT_URI"),
    });
    const cloudId = await fetchAccessibleCloudId(token.access_token);
    saveStoredTokens({
        accessToken: token.access_token,
        refreshToken: token.refresh_token,
        expiresAt: Date.now() + token.expires_in * 1000,
        cloudId,
    });
}

export function hasStoredOAuthSession(): boolean {
    return loadStoredTokens() !== null;
}

/** Returns a Confluence config that talks to the API gateway via Bearer auth, refreshing the access token if it's close to expiring. */
export async function confluenceConfigFromOAuth(): Promise<ConfluenceConfig> {
    const stored = loadStoredTokens();
    if (!stored) {
        throw new Error("Confluence is not authorized yet — visit GET /v1/sources/confluence/oauth/start first");
    }

    let accessToken = stored.accessToken;
    if (Date.now() >= stored.expiresAt - 60_000) {
        const refreshed = await postTokenRequest({
            grant_type: "refresh_token",
            client_id: requiredEnv("CONFLUENCE_CLIENT_ID"),
            client_secret: requiredEnv("CONFLUENCE_CLIENT_SECRET"),
            refresh_token: stored.refreshToken,
        });
        accessToken = refreshed.access_token;
        saveStoredTokens({
            accessToken: refreshed.access_token,
            refreshToken: refreshed.refresh_token,
            expiresAt: Date.now() + refreshed.expires_in * 1000,
            cloudId: stored.cloudId,
        });
    }

    return {
        baseUrl: `https://api.atlassian.com/ex/confluence/${stored.cloudId}`,
        authHeader: `Bearer ${accessToken}`,
    };
}
