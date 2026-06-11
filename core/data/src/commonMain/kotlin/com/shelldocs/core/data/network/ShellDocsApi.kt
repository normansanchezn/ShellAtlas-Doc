package com.shelldocs.core.data.network

import com.shelldocs.core.data.network.dto.CreateDocumentRequestDto
import com.shelldocs.core.data.network.dto.DocumentAttributesDto
import com.shelldocs.core.data.network.dto.DocumentDto
import com.shelldocs.core.data.network.dto.DocumentVersionDto
import com.shelldocs.core.data.network.dto.DraftReceiptDto
import com.shelldocs.core.data.network.dto.PublishDocumentRequestDto
import com.shelldocs.core.data.network.dto.SaveDraftRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess

/**
 * Client for the ShellDocs documents backend, preserving the original
 * ShellEnterpriseDoc `/v1` contract endpoint by endpoint.
 */
class ShellDocsApi(
    private val httpClient: HttpClient,
    private val config: ApiConfig,
) {

    suspend fun documents(): List<DocumentDto> =
        get("v1/documents").body()

    suspend fun document(id: String): DocumentDto =
        get("v1/documents/$id").body()

    suspend fun search(query: String): List<DocumentDto> =
        get("v1/search?q=${query.encodeURLParameter()}").body()

    suspend fun create(request: CreateDocumentRequestDto): DocumentDto =
        post("v1/documents", request).body()

    suspend fun publish(id: String, request: PublishDocumentRequestDto): DocumentDto =
        post("v1/documents/$id/publish", request).body()

    suspend fun saveDraft(id: String, request: SaveDraftRequestDto): DraftReceiptDto =
        post("v1/documents/$id/draft", request).body()

    suspend fun versions(id: String): List<DocumentVersionDto> =
        get("v1/documents/$id/versions").body()

    suspend fun restore(id: String, versionId: String): DocumentDto =
        postWithoutBody("v1/documents/$id/restore/$versionId").body()

    suspend fun updateAttributes(id: String, request: DocumentAttributesDto): DocumentDto =
        post("v1/documents/$id/attributes", request).body()

    suspend fun delete(id: String) {
        val response = httpClient.delete(url("v1/documents/$id")) { defaultHeaders() }
        response.ensureSuccess()
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

    private fun io.ktor.client.request.HttpRequestBuilder.defaultHeaders() {
        headers {
            config.bearerToken?.let { append("Authorization", "Bearer $it") }
        }
    }

    private fun HttpResponse.ensureSuccess() {
        if (status == HttpStatusCode.NotFound) throw ShellDocsApiException.NotFound
        if (status == HttpStatusCode.Unauthorized) throw ShellDocsApiException.Unauthorized
        if (!status.isSuccess()) throw ShellDocsApiException.Http(status.value)
    }
}
