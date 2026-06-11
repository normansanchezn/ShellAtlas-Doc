package com.shelldocs.core.domain.entity.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileTest {

    @Test
    fun initialsTakeFirstLettersOfFirstTwoNames() {
        val profile = profile(fullName = "Elena Vargas")
        assertEquals("EV", profile.initials)
    }

    @Test
    fun initialsFallBackToEmailWhenNameIsBlank() {
        val profile = profile(fullName = "  ")
        assertEquals("EL", profile.initials)
    }

    private fun profile(fullName: String) = UserProfile(
        id = "user-1",
        email = "elena.vargas@shell.com",
        fullName = fullName,
        team = "iOS Shell App",
        role = UserRole.OWNER,
    )
}
