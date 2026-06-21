package com.shelldocs.core.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionFieldDto(
    val status: String,
    val host: String? = null,
    val detail: String? = null,
)

/** Wire form of `GET /v1/connections/status`. */
@Serializable
data class ConnectionsStatusDto(
    val confluence: ConnectionFieldDto,
    val jira: ConnectionFieldDto,
    val azureDevops: ConnectionFieldDto,
    val database: ConnectionFieldDto,
)
