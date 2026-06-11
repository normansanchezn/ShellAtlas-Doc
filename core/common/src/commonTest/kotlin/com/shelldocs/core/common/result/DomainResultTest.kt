package com.shelldocs.core.common.result

import com.shelldocs.core.common.error.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainResultTest {

    @Test
    fun mapTransformsSuccessValue() {
        val result = DomainResult.success(2).map { it * 21 }
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun mapPreservesFailure() {
        val failure: DomainResult<Int> = DomainResult.failure(AppError.NotFound())
        val mapped = failure.map { it * 2 }
        assertTrue(mapped is DomainResult.Failure)
        assertNull(mapped.getOrNull())
    }

    @Test
    fun onSuccessAndOnFailureInvokeMatchingBranchOnly() {
        var successes = 0
        var failures = 0

        DomainResult.success(Unit)
            .onSuccess { successes++ }
            .onFailure { failures++ }
        DomainResult.failure(AppError.Network())
            .onSuccess { successes++ }
            .onFailure { failures++ }

        assertEquals(1, successes)
        assertEquals(1, failures)
    }

    @Test
    fun getOrDefaultFallsBackOnFailure() {
        val failure: DomainResult<String> = DomainResult.failure(AppError.Unknown())
        assertEquals("fallback", failure.getOrDefault("fallback"))
    }
}
