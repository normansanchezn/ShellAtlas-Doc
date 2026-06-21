package com.shelldocs.core.domain.entity.connection

/** Health of one system the app depends on, shown on the Connections screen. */
enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    DISABLED,
    ERROR,
}

data class ConnectionStatus(
    val id: String,
    val name: String,
    val state: ConnectionState,
    val detail: String? = null,
)
