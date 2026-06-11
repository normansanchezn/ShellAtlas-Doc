package com.shelldocs.core.common.id

/** Id factory abstraction so tests can produce deterministic identifiers. */
fun interface IdGenerator {
    fun newId(): String
}
