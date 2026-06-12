package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.network.ShellDocsApi
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
import kotlin.test.assertNotNull

class ApiDocumentRepositoryTest {

    private val documentJson = """
        {
          "id": "doc-1",
          "title": "Authentication",
          "summary": "Token behavior",
          "status": "published",
          "classification": "internal",
          "raw_markdown": "# Authentication",
          "content_json": { "schema_version": 1, "blocks": [] },
          "content_plaintext": "Authentication",
          "attributes": { "owner": "Elena Vargas", "module": "Authentication", "team": "iOS", "platform": "iOS", "tags": ["auth"] },
          "created_at": "2026-06-01T00:00:00Z",
          "updated_at": "2026-06-08T09:00:00Z"
        }
    """.trimIndent()

    private fun repository(handler: (io.ktor.client.request.HttpRequestData) -> Unit): ApiDocumentRepository {
        val engine = MockEngine { request ->
            handler(request)
            respond(
                content = when {
                    request.method == HttpMethod.Post && request.url.encodedPath.endsWith("/draft") ->
                        """{"document_id":"doc-1","content_hash":"hash-1","updated_at":"2026-06-12T10:00:00Z"}"""
                    request.method == HttpMethod.Get && request.url.encodedPath.endsWith("/documents") ->
                        "[$documentJson]"
                    else -> documentJson
                },
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return ApiDocumentRepository(ShellDocsApi(client, ApiConfig(baseUrl = "http://127.0.0.1:8787", bearerToken = "token-1")))
    }

    @Test
    fun saveDraftCallsTheDraftEndpoint() = runTest {
        val calls = mutableListOf<String>()
        val repository = repository { request ->
            calls += "${request.method.value} ${request.url.encodedPath}?${request.url.encodedQuery}"
            assertEquals("Bearer token-1", request.headers[HttpHeaders.Authorization])
        }

        val receipt = repository.saveDraft("doc-1", "# Draft body").getOrNull()

        assertNotNull(receipt)
        assertEquals("doc-1", receipt.documentId)
        assertEquals(1, calls.size)
        assertEquals("POST /v1/documents/doc-1/draft?", calls.first())
    }
}
