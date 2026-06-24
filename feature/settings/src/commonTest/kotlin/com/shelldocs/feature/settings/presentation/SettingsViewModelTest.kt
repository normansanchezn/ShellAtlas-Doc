package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.*
import com.shelldocs.core.domain.repository.AuthRepository
import com.shelldocs.core.domain.repository.RoleRepository
import com.shelldocs.core.domain.usecase.auth.AssignRoleUseCase
import com.shelldocs.core.domain.usecase.auth.GetTeamMembersUseCase
import com.shelldocs.core.domain.usecase.auth.SignOutUseCase
import com.shelldocs.core.domain.usecase.auth.UpdateLanguageUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private fun member(id: String, name: String, role: UserRole, current: Boolean = false) = TeamMember(
    profile = UserProfile(id, "$id@shell.com", name, "Team", role),
    isCurrentUser = current,
)

private class FakeRoleRepository : RoleRepository {
    val members = mutableMapOf(
        "user-1" to member("user-1", "Elena Vargas", UserRole.OWNER, current = true),
        "user-2" to member("user-2", "James O'Brien", UserRole.BUSINESS),
    )

    override suspend fun roleOf(userId: String) =
        DomainResult.success(members.getValue(userId).profile.role)

    override suspend fun teamMembers() = DomainResult.success(members.values.toList())

    override suspend fun assignRole(userId: String, role: UserRole): DomainResult<Unit> {
        val target = members.getValue(userId)
        members[userId] = target.copy(profile = target.profile.copy(role = role))
        return DomainResult.success(Unit)
    }
}

private class StubAuthRepository : AuthRepository {
    var signOuts = 0
    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession
    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> =
        DomainResult.failure(com.shelldocs.core.common.error.AppError.Unknown())
    override suspend fun signOut(): DomainResult<Unit> {
        signOuts++
        return DomainResult.success(Unit)
    }
    override suspend fun restoreSession(): DomainResult<AuthSession?> = DomainResult.success(null)
    override fun adoptSession(session: AuthSession) {
        mutableSession.value = session
    }
    override suspend fun updateLanguage(language: AppLanguage): DomainResult<UserProfile> =
        DomainResult.failure(com.shelldocs.core.common.error.AppError.Unauthorized())
}

class SettingsViewModelTest {

    private val roleRepository = FakeRoleRepository()
    private val authRepository = StubAuthRepository()
    private var role = UserRole.OWNER

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = SettingsViewModel(
        getTeamMembers = GetTeamMembersUseCase(roleRepository),
        assignRole = AssignRoleUseCase(roleRepository),
        signOut = SignOutUseCase(authRepository),
        updateLanguage = UpdateLanguageUseCase(authRepository),
        roleProvider = { role },
        languageProvider = { AppLanguage.ENGLISH },
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeLoadsMembersAndRole() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SettingsIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.currentState.members.size)
        assertEquals(true, viewModel.currentState.canManageMembers)
        viewModel.clear()
    }

    @Test
    fun ownerDelegatesRoleThroughTheTable() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SettingsIntent.Initialize)
        viewModel.onIntent(SettingsIntent.AssignRole("user-2", UserRole.DEVELOP))
        testScheduler.advanceUntilIdle()

        val updated = viewModel.currentState.members.first { it.profile.id == "user-2" }
        assertEquals(UserRole.DEVELOP, updated.profile.role)
        viewModel.clear()
    }

    @Test
    fun nonOwnerCannotDelegateRoles() = runTest {
        role = UserRole.DEVELOP
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SettingsIntent.Initialize)
        viewModel.onIntent(SettingsIntent.AssignRole("user-2", UserRole.OWNER))
        testScheduler.advanceUntilIdle()

        assertEquals(UserRole.BUSINESS, roleRepository.members.getValue("user-2").profile.role)
        assertNotNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun signOutEmitsEffect() = runTest {
        val viewModel = viewModel(testScheduler)
        var effect: SettingsEffect? = null
        val job = launch { effect = viewModel.effects.first() }
        testScheduler.runCurrent()

        viewModel.onIntent(SettingsIntent.SignOut)
        testScheduler.advanceUntilIdle()
        job.join()

        assertEquals(SettingsEffect.SignedOut, effect)
        assertEquals(1, authRepository.signOuts)
        viewModel.clear()
    }

    @Test
    fun togglesAreReflectedInState() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SettingsIntent.SetTwoFactorRequired(true))
        viewModel.onIntent(SettingsIntent.SetNotifyWeeklyDigest(true))
        testScheduler.advanceUntilIdle()

        assertEquals(true, viewModel.currentState.twoFactorRequired)
        assertEquals(true, viewModel.currentState.notifyWeeklyDigest)
        viewModel.clear()
    }
}
