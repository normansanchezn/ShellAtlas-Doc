package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.RoleRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class RecordingRoleRepository : RoleRepository {
    var assigned: Pair<String, UserRole>? = null

    override suspend fun roleOf(userId: String): DomainResult<UserRole> =
        DomainResult.success(UserRole.VIEWER)

    override suspend fun teamMembers(): DomainResult<List<TeamMember>> =
        DomainResult.success(emptyList())

    override suspend fun assignRole(userId: String, role: UserRole): DomainResult<Unit> {
        assigned = userId to role
        return DomainResult.success(Unit)
    }
}

class AssignRoleUseCaseTest {

    private val repository = RecordingRoleRepository()
    private val useCase = AssignRoleUseCase(repository)

    @Test
    fun ownerCanDelegateRoles() = runTest {
        val result = useCase(actorRole = UserRole.OWNER, targetUserId = "user-2", newRole = UserRole.DEVELOP)

        assertIs<DomainResult.Success<Unit>>(result)
        assertEquals("user-2" to UserRole.DEVELOP, repository.assigned)
    }

    @Test
    fun nonOwnersAreRejectedBeforeReachingRepository() = runTest {
        listOf(UserRole.DEVELOP, UserRole.BUSINESS, UserRole.VIEWER).forEach { actor ->
            val result = useCase(actorRole = actor, targetUserId = "user-2", newRole = UserRole.OWNER)
            val failure = assertIs<DomainResult.Failure>(result)
            assertIs<AppError.Unauthorized>(failure.error)
        }
        assertNull(repository.assigned)
    }
}
