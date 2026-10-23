package kz.mtemirbay.shop.feature.account.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import kz.mtemirbay.shop.core.model.AuthField
import kz.mtemirbay.shop.core.model.AuthValidation
import kz.mtemirbay.shop.core.model.AuthValidationError
import kz.mtemirbay.shop.core.ui.COUNTRY_CODE
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.core.ui.phoneDigitsFromInput
import kz.mtemirbay.shop.feature.account.domain.AuthRejectedException
import kz.mtemirbay.shop.feature.account.domain.AuthRepository

/**
 * Вход и регистрация — одна форма с переключателем: поля почти те же.
 * После успеха ничего никуда не шлём: сессия сохранена, и экран профиля переключится сам, подписавшись на неё
 */
internal class AuthViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<AuthState, AuthEvent, Nothing>(AuthState()) {

    private var visibleErrorFields = emptySet<AuthField>()

    override fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.ModeChanged -> {
                visibleErrorFields = emptySet()
                setState { copy(mode = event.mode, formError = null) }
                refreshErrors()
            }
            is AuthEvent.NameChanged -> onFieldChanged { copy(name = event.value) }
            is AuthEvent.PhoneChanged -> onFieldChanged { copy(phone = phoneDigitsFromInput(event.value, phone)) }
            is AuthEvent.PasswordChanged -> onFieldChanged { copy(password = event.value) }
            is AuthEvent.FieldFocusLost -> {
                visibleErrorFields = visibleErrorFields + event.field
                refreshErrors()
            }
            AuthEvent.Submit -> submit()
        }
    }

    private fun onFieldChanged(change: AuthState.() -> AuthState) {
        setState { change().copy(formError = null) }
        refreshErrors()
    }

    private fun submit() {
        if (state.value.isSubmitting) return
        visibleErrorFields = AuthField.entries.toSet()
        refreshErrors()
        if (localErrors(state.value).isNotEmpty()) return

        setState { copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            val current = state.value
            val phone = COUNTRY_CODE + current.phone
            val result = when (current.mode) {
                AuthMode.LOGIN -> authRepository.login(phone, current.password)
                AuthMode.REGISTER -> authRepository.register(current.name, phone, current.password)
            }
            setState { copy(isSubmitting = false, formError = result.exceptionOrNull()?.toFormError()) }
        }
    }

    private fun Throwable.toFormError(): AuthFormError = when ((this as? AuthRejectedException)?.code) {
        AuthErrorCodes.INVALID_CREDENTIALS -> AuthFormError.INVALID_CREDENTIALS
        AuthErrorCodes.PHONE_TAKEN -> AuthFormError.PHONE_TAKEN
        // Ошибку валидации сервера здесь не ждём — форма проверяет то же самое. Если пришла, показываем общую
        else -> AuthFormError.NETWORK
    }

    private fun refreshErrors() {
        setState { copy(errors = localErrors(this).filterKeys { it in visibleErrorFields }) }
    }

    private fun localErrors(state: AuthState): Map<AuthField, AuthValidationError> = listOfNotNull(
        if (state.mode == AuthMode.REGISTER) AuthValidation.validateName(state.name) else null,
        AuthValidation.validatePhone(COUNTRY_CODE + state.phone),
        // При входе длину пароля не проверяем: решает сервер. Иначе пароль старого формата нельзя было бы ввести
        if (state.mode == AuthMode.REGISTER) AuthValidation.validatePassword(state.password) else null,
    ).associateBy { it.field }
}
