package com.shelldocs.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShellDocsApiTest {

    private val documentJson = """
        {
          "id": "doc-1",
          "title": "Authentication",
          "summary": "Token behavior",
          "status": "published",
          "classification": "internal",
          "raw_markdown": "# Authentication",
          "content_json": { "schema_version": 1, "blocks": [ { "type": "heading", "level": 1, "text": "Authentication" } ] },
          "content_plaintext": "Authentication",
          "attributes": { "owner": "Elena Vargas", "module": "Authentication", "team": "iOS", "platform": "iOS", "tags": ["auth"] },
          "created_at": "2026-06-01T00:00:00Z",
          "updated_at": "2026-06-08T09:00:00Z"
        }
    """.trimIndent()

    private fun api(handler: MockEngine.() -> Unit = {}, status: HttpStatusCode = HttpStatusCode.OK, body: String = "[$documentJson]"): ShellDocsApi {
        val engine = MockEngine { request ->
            assertEquals("Bearer token-1", request.headers[HttpHeaders.Authorization])
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return ShellDocsApi(client, ApiConfig(baseUrl = "http://127.0.0.1:8787", bearerToken = "token-1"))
    }

    @Test
    fun documentsAreFetchedAndDecoded() = runTest {
        val documents = api().documents()

        assertEquals(1, documents.size)
        assertEquals("doc-1", documents.first().id)
        assertEquals("Authentication", documents.first().contentJson.blocks.first().text)
    }

    @Test
    fun notFoundBecomesTypedException() = runTest {
        val failing = api(status = HttpStatusCode.NotFound, body = "{}")

        assertFailsWith<ShellDocsApiException.NotFound> { failing.document("missing") }
    }

    @Test
    fun unauthorizedBecomesTypedException() = runTest {
        val failing = api(status = HttpStatusCode.Unauthorized, body = "{}")

        assertFailsWith<ShellDocsApiException.Unauthorized> { failing.documents() }
    }
}
