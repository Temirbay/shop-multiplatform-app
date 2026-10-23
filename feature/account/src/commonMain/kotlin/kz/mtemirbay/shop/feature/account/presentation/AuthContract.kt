package kz.mtemirbay.shop.feature.account.presentation

import kz.mtemirbay.shop.core.model.AuthField
import kz.mtemirbay.shop.core.model.AuthValidationError

internal enum class AuthMode { LOGIN, REGISTER }

/** Ошибка формы целиком, а не конкретного поля */
internal enum class AuthFormError { INVALID_CREDENTIALS, PHONE_TAKEN, NETWORK }

internal data class AuthState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    /** 10 цифр номера, маску рисует PhoneVisualTransformation */
    val phone: String = "",
    val password: String = "",
    val errors: Map<AuthField, AuthValidationError> = emptyMap(),
    val formError: AuthFormError? = null,
    val isSubmitting: Boolean = false,
)

internal sealed interface AuthEvent {
    data class ModeChanged(val mode: AuthMode) : AuthEvent
    data class NameChanged(val value: String) : AuthEvent
    data class PhoneChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data class FieldFocusLost(val field: AuthField) : AuthEvent
    data object Submit : AuthEvent
}
