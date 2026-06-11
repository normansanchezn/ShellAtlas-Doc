package com.shelldocs.core.data.network

/** Connection settings for the ShellDocs documents backend (`/v1`). */
data class ApiConfig(
    val baseUrl: String,
    val bearerToken: String? = null,
)
