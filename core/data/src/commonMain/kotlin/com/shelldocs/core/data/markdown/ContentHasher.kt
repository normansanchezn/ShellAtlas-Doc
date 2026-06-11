package com.shelldocs.core.data.markdown

/**
 * Stable FNV-1a 64-bit content hash, used for draft deduplication.
 * (The backend keeps SHA-256; this hash is only a client-side fingerprint.)
 */
object ContentHasher {

    private const val FNV_OFFSET_BASIS = -0x340d631b7bdddcdbL
    private const val FNV_PRIME = 0x100000001b3L

    fun hash(content: String): String {
        var hash = FNV_OFFSET_BASIS
        content.encodeToByteArray().forEach { byte ->
            hash = hash xor (byte.toLong() and 0xff)
            hash *= FNV_PRIME
        }
        return hash.toULong().toString(16).padStart(16, '0')
    }
}
