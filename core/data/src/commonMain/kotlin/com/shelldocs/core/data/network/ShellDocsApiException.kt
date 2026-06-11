package com.shelldocs.core.data.network

/** Typed transport failures raised by [ShellDocsApi]. */
sealed class ShellDocsApiException(message: String) : Exception(message) {

    data object NotFound : ShellDocsApiException("Resource not found") {
        private fun readResolve(): Any = NotFound
    }

    data object Unauthorized : ShellDocsApiException("Unauthorized") {
        private fun readResolve(): Any = Unauthorized
    }

    class Http(val statusCode: Int) : ShellDocsApiException("HTTP $statusCode")
}
