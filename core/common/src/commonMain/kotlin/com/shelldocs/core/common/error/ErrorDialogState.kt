package com.shelldocs.core.common.error

/**
 * User-facing error content rendered in a modal dialog.
 * Messages should be descriptive and free of technical implementation details.
 */
data class ErrorDialogState(
    val title: String,
    val message: String,
    val confirmLabel: String = "OK",
)

fun AppError.toErrorDialogState(actionLabel: String? = null): ErrorDialogState = when (this) {
    is AppError.Network -> ErrorDialogState(
        title = "We couldn't complete your request",
        message = actionLabel?.let {
            "We couldn't $it because the connection is unavailable right now. Please try again in a moment."
        } ?: "The connection is unavailable right now. Please try again in a moment.",
    )
    is AppError.Unauthorized -> ErrorDialogState(
        title = "You don't have access to do that",
        message = actionLabel?.let {
            "We couldn't $it with your current access level. If you think this should be available, contact your administrator."
        } ?: "This action isn't available with your current access level. If you think this should be available, contact your administrator.",
    )
    is AppError.NotFound -> ErrorDialogState(
        title = "We couldn't find what you were looking for",
        message = actionLabel?.let {
            "We couldn't $it because the information is no longer available or has already changed."
        } ?: "The information is no longer available or may have changed.",
    )
    is AppError.Validation -> ErrorDialogState(
        title = "Please review the information",
        message = message,
    )
    is AppError.Conflict -> ErrorDialogState(
        title = "This changed before it could be completed",
        message = actionLabel?.let {
            "We couldn't $it because the content changed during the process. Refresh the information and try again."
        } ?: "The content changed during the process. Refresh the information and try again.",
    )
    is AppError.Unknown -> ErrorDialogState(
        title = "Something went wrong",
        message = actionLabel?.let {
            "We couldn't $it due to an unexpected problem. Please try again."
        } ?: "An unexpected problem occurred. Please try again.",
    )
}
