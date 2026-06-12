@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.source.SourceStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DemoSourcesRepositoryTest {

    private val repository = DemoSourcesRepository(TimeProvider { DemoSeed.now })

    @Test
    fun syncingABrokenSourceIsRejected() = runTest {
        val result = repository.sync("source-jira")
        assertIs<DomainResult.Failure>(result)
    }

    @Test
    fun reconnectClearsErrorAndAllowsSync() = runTest {
        val reconnected = repository.reconnect("source-jira").getOrNull()

        assertNotNull(reconnected)
        assertEquals(SourceStatus.CONNECTED, reconnected.status)
        assertNull(reconnected.errorMessage)

        val synced = repository.sync("source-jira").getOrNull()
        assertNotNull(synced)
        assertEquals(DemoSeed.now, synced.lastSyncAt)
    }

    @Test
    fun successfulSyncAppendsToTheLog() = runTest {
        val before = repository.syncLog().getOrDefault(emptyList()).size
        repository.sync("source-confluence")
        val after = repository.syncLog().getOrDefault(emptyList()).size

        assertEquals(before + 1, after)
    }
}
