package kz.mtemirbay.shop.feature.account.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import kz.mtemirbay.shop.core.model.AuthField
import kz.mtemirbay.shop.core.model.AuthValidationError
import kz.mtemirbay.shop.feature.account.domain.AuthRejectedException
import kz.mtemirbay.shop.feature.account.domain.AuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val repository = FakeAuthRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loginSendsNormalizedPhone() = runTest {
        val viewModel = AuthViewModel(repository).fillLogin()

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(listOf("login +77011234567 password1"), repository.calls)
        assertNull(viewModel.state.value.formError)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun invalidCredentialsShowFormError() = runTest {
        repository.result = Result.failure(AuthRejectedException(AuthErrorCodes.INVALID_CREDENTIALS))
        val viewModel = AuthViewModel(repository).fillLogin()

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthFormError.INVALID_CREDENTIALS, viewModel.state.value.formError)
    }

    @Test
    fun editingFieldClearsFormError() = runTest {
        repository.result = Result.failure(AuthRejectedException(AuthErrorCodes.INVALID_CREDENTIALS))
        val viewModel = AuthViewModel(repository).fillLogin()
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.PasswordChanged("password2"))

        assertNull(viewModel.state.value.formError)
    }

    @Test
    fun registerValidatesNameAndPasswordLength() = runTest {
        val viewModel = AuthViewModel(repository)
        viewModel.onEvent(AuthEvent.ModeChanged(AuthMode.REGISTER))
        viewModel.onEvent(AuthEvent.PhoneChanged("7011234567"))
        viewModel.onEvent(AuthEvent.PasswordChanged("short"))

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(
            mapOf(
                AuthField.NAME to AuthValidationError.NAME_EMPTY,
                AuthField.PASSWORD to AuthValidationError.PASSWORD_TOO_SHORT,
            ),
            viewModel.state.value.errors,
        )
        assertEquals(emptyList(), repository.calls)
    }

    @Test
    fun loginDoesNotCheckPasswordLength() = runTest {
        val viewModel = AuthViewModel(repository)
        viewModel.onEvent(AuthEvent.PhoneChanged("7011234567"))
        viewModel.onEvent(AuthEvent.PasswordChanged("short"))

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.calls.size)
    }

    @Test
    fun takenPhoneOnRegisterShowsFormError() = runTest {
        repository.result = Result.failure(AuthRejectedException(AuthErrorCodes.PHONE_TAKEN))
        val viewModel = AuthViewModel(repository)
        viewModel.onEvent(AuthEvent.ModeChanged(AuthMode.REGISTER))
        viewModel.onEvent(AuthEvent.NameChanged("Мирас"))
        viewModel.onEvent(AuthEvent.PhoneChanged("7011234567"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password1"))

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(listOf("register Мирас +77011234567 password1"), repository.calls)
        assertEquals(AuthFormError.PHONE_TAKEN, viewModel.state.value.formError)
    }

    @Test
    fun networkErrorShowsNetworkFormError() = runTest {
        repository.result = Result.failure(RuntimeException("Network"))
        val viewModel = AuthViewModel(repository).fillLogin()

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthFormError.NETWORK, viewModel.state.value.formError)
    }

    private fun AuthViewModel.fillLogin() = apply {
        onEvent(AuthEvent.PhoneChanged("+7 701 123 45 67"))
        onEvent(AuthEvent.PasswordChanged("password1"))
    }

    private class FakeAuthRepository : AuthRepository {
        var result: Result<Unit> = Result.success(Unit)
        val calls = mutableListOf<String>()

        override suspend fun login(phone: String, password: String): Result<Unit> {
            calls += "login $phone $password"
            return result
        }

        override suspend fun register(name: String, phone: String, password: String): Result<Unit> {
            calls += "register $name $phone $password"
            return result
        }
    }
}
