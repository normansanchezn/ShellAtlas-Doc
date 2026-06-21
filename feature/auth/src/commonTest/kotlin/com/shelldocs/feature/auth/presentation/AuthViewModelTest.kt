@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.*
import com.shelldocs.core.domain.repository.AuthRepository
import com.shelldocs.core.domain.usecase.auth.SignInUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

private class StubAuthRepository(var result: DomainResult<AuthSession>) : AuthRepository {
    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession
    override suspend fun signIn(credentials: SignInCredentials) = result
    override suspend fun signOut() = DomainResult.success(Unit)
    override suspend fun restoreSession(): DomainResult<AuthSession?> = DomainResult.success(null)
    override suspend fun updateLanguage(language: AppLanguage): DomainResult<UserProfile> =
        DomainResult.failure(AppError.Unauthorized())
}

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private fun sampleSession() = AuthSession(
    accessToken = "at",
    refreshToken = "rt",
    expiresAt = Instant.parse("2030-06-12T00:00:00Z"),
    user = UserProfile("user-1", "elena.vargas@shell.com", "Elena Vargas", "iOS Shell App", UserRole.OWNER),
)

class AuthViewModelTest {

    private fun viewModel(
        repository: StubAuthRepository,
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
    ) = AuthViewModel(
        signIn = SignInUseCase(repository),
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun typingUpdatesStateAndClearsErrors() = runTest {
        val viewModel = viewModel(StubAuthRepository(DomainResult.success(sampleSession())), testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("elena.vargas@shell.com"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret-123"))
        testScheduler.advanceUntilIdle()

        assertEquals("elena.vargas@shell.com", viewModel.currentState.email)
        assertTrue(viewModel.currentState.canSubmit)
        viewModel.clear()
    }

    @Test
    fun successfulSubmitEmitsNavigationEffect() = runTest {
        val viewModel = viewModel(StubAuthRepository(DomainResult.success(sampleSession())), testScheduler)
        var navigated: AuthEffect? = null
        val job = launch { navigated = viewModel.effects.first() }
        testScheduler.runCurrent()

        viewModel.onIntent(AuthIntent.EmailChanged("elena.vargas@shell.com"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret-123"))
        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()
        job.join()

        assertEquals(AuthEffect.NavigateToWorkspace, navigated)
        assertFalse(viewModel.currentState.isLoading)
        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun validationFailureSurfacesErrorWithoutNavigation() = runTest {
        val viewModel = viewModel(StubAuthRepository(DomainResult.success(sampleSession())), testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("not-an-email"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret-123"))
        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)
        assertFalse(viewModel.currentState.isLoading)
        viewModel.clear()
    }

    @Test
    fun unauthorizedFailureShowsRepositoryMessage() = runTest {
        val repository = StubAuthRepository(DomainResult.failure(AppError.Unauthorized("Invalid credentials")))
        val viewModel = viewModel(repository, testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("elena.vargas@shell.com"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret-123"))
        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()

        assertEquals("We couldn't sign you in", viewModel.currentState.errorDialog?.title)
        assertEquals("Check your email and password and try again.", viewModel.currentState.errorDialog?.message)
        viewModel.clear()
    }

    @Test
    fun networkFailureExplainsLocalSupabaseFallback() = runTest {
        val repository = StubAuthRepository(DomainResult.failure(AppError.Network("Could not reach Supabase")))
        val viewModel = viewModel(repository, testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("elena.vargas@shell.com"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret-123"))
        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()

        assertEquals("We couldn't sign you in", viewModel.currentState.errorDialog?.title)
        assertEquals(
            "The sign-in service is unavailable right now. If you're working locally, start Supabase or clear the Supabase env vars to use demo mode.",
            viewModel.currentState.errorDialog?.message,
        )
        viewModel.clear()
    }

    @Test
    fun submitIsIgnoredWhileFieldsAreEmpty() = runTest {
        val viewModel = viewModel(StubAuthRepository(DomainResult.success(sampleSession())), testScheduler)

        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.currentState.isLoading)
        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun canSubmitRequiresBothFields() = runTest {
        val viewModel = viewModel(StubAuthRepository(DomainResult.success(sampleSession())), testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("user@shell.com"))
        assertFalse(viewModel.currentState.canSubmit)

        viewModel.onIntent(AuthIntent.PasswordChanged("secret"))
        assertTrue(viewModel.currentState.canSubmit)

        viewModel.onIntent(AuthIntent.PasswordChanged(""))
        assertFalse(viewModel.currentState.canSubmit)
        viewModel.clear()
    }

    @Test
    fun dismissErrorClearsDialog() = runTest {
        val repository = StubAuthRepository(DomainResult.failure(AppError.Unauthorized("bad creds")))
        val viewModel = viewModel(repository, testScheduler)

        viewModel.onIntent(AuthIntent.EmailChanged("user@shell.com"))
        viewModel.onIntent(AuthIntent.PasswordChanged("wrong"))
        viewModel.onIntent(AuthIntent.Submit)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)

        viewModel.onIntent(AuthIntent.DismissError)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }
}
