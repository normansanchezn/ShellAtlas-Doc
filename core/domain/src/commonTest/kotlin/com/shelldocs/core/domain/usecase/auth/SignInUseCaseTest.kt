package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.fixtures.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignInUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = SignInUseCase(repository)

    @Test
    fun rejectsMalformedEmailWithoutHittingRepository() = runTest {
        val result = useCase(SignInCredentials(email = "not-an-email", password = "secret-123"))

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertNull(repository.lastCredentials)
    }

    @Test
    fun rejectsShortPassword() = runTest {
        val result = useCase(SignInCredentials(email = "elena.vargas@shell.com", password = "short"))

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }

    @Test
    fun trimsEmailAndDelegatesToRepository() = runTest {
        val result = useCase(
            SignInCredentials(email = "  elena.vargas@shell.com ", password = "secret-123"),
        )

        assertTrue(result is DomainResult.Success)
        assertEquals("elena.vargas@shell.com", repository.lastCredentials?.email)
    }

    @Test
    fun propagatesRepositoryFailure() = runTest {
        repository.nextResult = FakeAuthRepository.unauthorized()

        val result = useCase(SignInCredentials(email = "elena.vargas@shell.com", password = "secret-123"))

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Unauthorized>(failure.error)
    }
}
