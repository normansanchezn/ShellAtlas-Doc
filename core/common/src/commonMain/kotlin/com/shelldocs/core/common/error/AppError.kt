package com.shelldocs.core.common.error

/**
 * Canonical, transport-agnostic error taxonomy shared by every layer.
 */
sealed interface AppError {

    val message: String

    data class Network(override val message: String = "Network unavailable") : AppError

    data class Unauthorized(override val message: String = "Session expired or invalid credentials") : AppError

    data class NotFound(override val message: String = "Resource not found") : AppError

    data class Validation(override val message: String) : AppError

    data class Conflict(override val message: String = "The resource changed concurrently") : AppError

    data class Unknown(override val message: String = "Unexpected error") : AppError
}
