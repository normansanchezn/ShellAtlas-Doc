package com.shelldocs.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppNavigatorTest {

    @Test
    fun startsOnAssistant() {
        assertEquals(AppRoute.ASSISTANT, AppNavigator().route.value)
    }

    @Test
    fun navigateReplacesRouteAndBackReturns() {
        val navigator = AppNavigator()

        navigator.navigate(AppRoute.DASHBOARD)
        assertEquals(AppRoute.DASHBOARD, navigator.route.value)

        assertTrue(navigator.navigateBack())
        assertEquals(AppRoute.ASSISTANT, navigator.route.value)
    }

    @Test
    fun backWithoutHistoryIsNotConsumed() {
        assertFalse(AppNavigator().navigateBack())
    }

    @Test
    fun navigatingToCurrentRouteDoesNotPolluteHistory() {
        val navigator = AppNavigator()
        navigator.navigate(AppRoute.ASSISTANT)
        assertFalse(navigator.navigateBack())
    }
}
