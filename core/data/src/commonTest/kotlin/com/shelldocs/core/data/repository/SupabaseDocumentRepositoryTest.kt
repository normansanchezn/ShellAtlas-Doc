@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.supabase.SupabaseConfig
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SupabaseDocumentRepositoryTest {

    private val config = SupabaseConfig(url = "https://demo.supabase.co", anonKey = "anon-key")
    private val now = kotlin.time.Instant.parse("2026-06-12T10:00:00Z")

    private fun repository(
        handler: (io.ktor.client.request.HttpRequestData) -> Unit,
        currentUserIdProvider: () -> String? = { "user-1" },
    ): SupabaseDocumentRepository {
        val engine = MockEngine { request ->
            handler(request)
            val body = when {
                request.method == HttpMethod.Get && request.url.encodedPath.endsWith("/documents") -> """
                    [{
                      "id": "doc-1",
                      "title": "Authentication",
                      "slug": "authentication",
                      "status": "published",
                      "classification": "internal",
                      "current_version_id": "ver-1",
                      "created_at": "2026-06-01T00:00:00Z",
                      "updated_at": "2026-06-11T00:00:00Z"
                    }]
                """.trimIndent()
                request.method == HttpMethod.Get && request.url.encodedPath.endsWith("/document_versions") -> """
                    [{
                      "id": "ver-1",
                      "document_id": "doc-1",
                      "version_number": 1,
                      "title": "Authentication",
                      "raw_markdown": "# Authentication",
                      "content_json": {"schema_version": 1, "blocks": []},
                      "content_plaintext": "Authentication",
                      "content_hash": "hash-1",
                      "change_summary": "Initial version",
                      "source_version": null,
                      "created_at": "2026-06-11T00:00:00Z"
                    }]
                """.trimIndent()
                request.method == HttpMethod.Get && request.url.encodedPath.endsWith("/document_attributes") -> """
                    [
                      {"document_id": "doc-1", "key": "owner", "value": "Elena Vargas"},
                      {"document_id": "doc-1", "key": "module", "value": "Authentication"},
                      {"document_id": "doc-1", "key": "team", "value": "iOS"},
                      {"document_id": "doc-1", "key": "platform", "value": "iOS"},
                      {"document_id": "doc-1", "key": "tags", "value": ["auth"]}
                    ]
                """.trimIndent()
                else -> "[]"
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val postgrest = SupabasePostgrestApi(client, config) { "user-1" }
        return SupabaseDocumentRepository(
            postgrest = postgrest,
            timeProvider = TimeProvider { now },
            currentUserIdProvider = currentUserIdProvider,
        )
    }

    @Test
    fun saveDraftUsesDocumentDraftsTableAndCurrentUser() = runTest {
        val calls = mutableListOf<String>()
        val repository = repository(handler = { request ->
            calls += "${request.method.value} ${request.url.encodedPath}?${request.url.encodedQuery}"
            assertEquals("anon-key", request.headers["apikey"])
            assertEquals("Bearer user-1", request.headers[HttpHeaders.Authorization])
        })

        val receipt = repository.saveDraft("doc-1", "# Draft body").getOrNull()

        assertNotNull(receipt)
        assertEquals("doc-1", receipt.documentId)
        assertEquals(now, receipt.savedAt)
        assertEquals(2, calls.size)
        assertEquals("GET /rest/v1/documents?select=id,title,slug,status,classification,current_version_id,created_at,updated_at&id=eq.doc-1&deleted_at=is.null", calls[0])
        assertEquals("POST /rest/v1/document_drafts?", calls[1])
    }

    @Test
    fun saveDraftFailsWithoutCurrentUser() = runTest {
        val repository = repository(
            handler = { request ->
                assertEquals("anon-key", request.headers["apikey"])
            },
            currentUserIdProvider = { null },
        )

        val result = repository.saveDraft("doc-1", "# Draft body")

        val error = assertIs<DomainResult.Failure>(result).error
        assertIs<AppError.Validation>(error)
    }
}
