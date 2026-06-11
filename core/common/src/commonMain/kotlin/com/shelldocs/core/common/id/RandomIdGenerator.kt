package com.shelldocs.core.common.id

import kotlin.random.Random

/** Production [IdGenerator]: 16 hex chars of randomness. */
class RandomIdGenerator : IdGenerator {
    override fun newId(): String =
        buildString {
            repeat(16) { append(HEX[Random.nextInt(HEX.length)]) }
        }

    private companion object {
        const val HEX = "0123456789abcdef"
    }
}
