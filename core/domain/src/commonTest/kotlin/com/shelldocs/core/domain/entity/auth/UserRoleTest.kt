package com.shelldocs.core.domain.entity.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoleTest {

    @Test
    fun fromKeyMatchesSupabaseKeysCaseInsensitively() {
        assertEquals(UserRole.OWNER, UserRole.fromKey("owner"))
        assertEquals(UserRole.BUSINESS, UserRole.fromKey("Business"))
        assertEquals(UserRole.DEVELOP, UserRole.fromKey("DEVELOP"))
    }

    @Test
    fun unknownOrNullKeysDegradeToViewer() {
        assertEquals(UserRole.VIEWER, UserRole.fromKey("superadmin"))
        assertEquals(UserRole.VIEWER, UserRole.fromKey(null))
    }
}
