package com.shelldocs.core.data.network

import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import com.shelldocs.core.data.network.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Client for the ShellDocs documents backend, preserving the original
 * ShellEnterpriseDoc `/v1` contract endpoint by endpoint.
 */
class ShellDocsApi(
    private val httpClient: HttpClient,
    private val config: ApiConfig,
) {
    private val logger = AppLogger.tag(LogTags.CRUD)

    suspend fun documents(): List<DocumentDto> {
        logger.d("READ v1/documents")
        return get("v1/documents").body()
    }

    suspend fun document(id: String): DocumentDto {
        logger.d("READ v1/documents/$id")
        return get("v1/documents/$id").body()
    }

    suspend fun search(query: String): List<DocumentDto> {
        logger.d("READ v1/search?q=$query")
        return get("v1/search?q=${query.encodeURLParameter()}").body()
    }

    suspend fun create(request: CreateDocumentRequestDto): DocumentDto {
        logger.i("CREATE v1/documents")
        return post("v1/documents", request).body<DocumentDto>()
            .also { logger.i("CREATE v1/documents succeeded id=${it.id}") }
    }

    suspend fun publish(id: String, request: PublishDocumentRequestDto): DocumentDto {
        logger.i("UPDATE v1/documents/$id/publish")
        return post("v1/documents/$id/publish", request).body()
    }

    suspend fun saveDraft(id: String, request: SaveDraftRequestDto): DraftReceiptDto {
        logger.i("UPDATE v1/documents/$id/draft")
        return post("v1/documents/$id/draft", request).body()
    }

    suspend fun versions(id: String): List<DocumentVersionDto> {
        logger.d("READ v1/documents/$id/versions")
        return get("v1/documents/$id/versions").body()
    }

    suspend fun restore(id: String, versionId: String): DocumentDto {
        logger.i("UPDATE v1/documents/$id/restore/$versionId")
        return postWithoutBody("v1/documents/$id/restore/$versionId").body()
    }

    suspend fun updateAttributes(id: String, request: DocumentAttributesDto): DocumentDto {
        logger.i("UPDATE v1/documents/$id/attributes")
        return post("v1/documents/$id/attributes", request).body()
    }

    suspend fun connectionsStatus(): ConnectionsStatusDto {
        logger.d("READ v1/connections/status")
        return get("v1/connections/status").body()
    }

    suspend fun delete(id: String) {
        logger.i("DELETE v1/documents/$id")
        val response = httpClient.delete(url("v1/documents/$id")) { defaultHeaders() }
        runCatching { response.ensureSuccess() }
            .onSuccess { logger.i("DELETE v1/documents/$id succeeded") }
            .onFailure { error -> logger.e("DELETE v1/documents/$id failed: ${error.message}", error) }
            .getOrThrow()
    }

    private suspend fun get(path: String): HttpResponse =
        httpClient.get(url(path)) { defaultHeaders() }.also { it.ensureSuccess() }

    private suspend inline fun <reified Body> post(path: String, body: Body?): HttpResponse =
        httpClient.post(url(path)) {
            defaultHeaders()
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.also { it.ensureSuccess() }

    private suspend fun postWithoutBody(path: String): HttpResponse =
        httpClient.post(url(path)) { defaultHeaders() }.also { it.ensureSuccess() }

    private fun url(path: String): String = "${config.baseUrl.trimEnd('/')}/$path"

    private fun HttpRequestBuilder.defaultHeaders() {
        headers {
            config.bearerToken?.let { append("Authorization", "Bearer $it") }
        }
    }

    private fun HttpResponse.ensureSuccess() {
        if (status.isSuccess()) return
        logger.e("${request.method.value} ${request.url} failed with ${status.value}")
        if (status == HttpStatusCode.NotFound) throw ShellDocsApiException.NotFound
        if (status == HttpStatusCode.Unauthorized) throw ShellDocsApiException.Unauthorized
        throw ShellDocsApiException.Http(status.value)
    }
}
