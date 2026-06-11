package com.shelldocs.core.data.assistant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/** Minimal Ollama HTTP client (`/api/generate`, `/api/tags`). */
class OllamaClient(
    private val httpClient: HttpClient,
    private val config: OllamaConfig,
) {

    suspend fun generate(prompt: String): String {
        val response = httpClient.post("${config.baseUrl.trimEnd('/')}/api/generate") {
            contentType(ContentType.Application.Json)
            setBody(OllamaGenerateRequestDto(model = config.model, prompt = prompt))
        }
        check(response.status.isSuccess()) { "Ollama returned ${response.status.value}" }
        return response.body<OllamaGenerateResponseDto>().response
    }

    suspend fun isReachable(): Boolean = runCatching {
        httpClient.get("${config.baseUrl.trimEnd('/')}/api/tags").status.isSuccess()
    }.getOrDefault(false)

    val modelName: String get() = config.model
}
