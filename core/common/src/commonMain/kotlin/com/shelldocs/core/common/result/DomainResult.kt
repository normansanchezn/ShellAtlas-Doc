package com.shelldocs.core.common.result

import com.shelldocs.core.common.error.AppError

/**
 * Explicit success/failure channel for every boundary crossing
 * (use case -> repository -> data source). Avoids leaking exceptions
 * across layers and keeps failures exhaustively handled.
 */
sealed interface DomainResult<out T> {

    data class Success<T>(val value: T) : DomainResult<T>

    data class Failure(val error: AppError) : DomainResult<Nothing>

    companion object {
        fun <T> success(value: T): DomainResult<T> = Success(value)
        fun failure(error: AppError): DomainResult<Nothing> = Failure(error)
    }
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(value)
    return this
}

inline fun <T> DomainResult<T>.onFailure(action: (AppError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) action(error)
    return this
}

fun <T> DomainResult<T>.getOrNull(): T? = (this as? DomainResult.Success)?.value

fun <T> DomainResult<T>.getOrDefault(default: T): T = getOrNull() ?: default
