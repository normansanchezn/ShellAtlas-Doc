package com.shelldocs.app.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppEnvironmentTest {

    @Test
    fun placeholderValuesAreIgnored() {
        assertNull(normalizeRuntimeSetting("your-dev-anon-key"))
        assertNull(normalizeRuntimeSetting("https://your-project.supabase.co"))
        assertNull(normalizeRuntimeSetting("your.name@example.com"))
    }

    @Test
    fun realValuesArePreserved() {
        assertEquals("http://127.0.0.1:54321", normalizeRuntimeSetting("http://127.0.0.1:54321"))
        assertEquals("sb_publishable_xxx", normalizeRuntimeSetting("sb_publishable_xxx"))
    }
}
