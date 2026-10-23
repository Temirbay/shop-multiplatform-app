package kz.mtemirbay.shop.core.model

enum class AuthField { NAME, PHONE, PASSWORD }

enum class AuthValidationError(val field: AuthField) {
    NAME_EMPTY(AuthField.NAME),
    NAME_TOO_LONG(AuthField.NAME),
    PHONE_INVALID(AuthField.PHONE),
    PASSWORD_TOO_SHORT(AuthField.PASSWORD),
    PASSWORD_TOO_LONG(AuthField.PASSWORD),
}

/** Как и [OrderValidation] — один код для формы в приложении и для сервера */
object AuthValidation {

    const val PASSWORD_MIN_LENGTH = 8

    /** bcrypt учитывает только первые 72 байта пароля — длиннее не принимаем, чтобы не было сюрпризов */
    const val PASSWORD_MAX_LENGTH = 64

    fun validateName(name: String): AuthValidationError? = when (OrderValidation.validateName(name)) {
        OrderValidationError.NAME_EMPTY -> AuthValidationError.NAME_EMPTY
        OrderValidationError.NAME_TOO_LONG -> AuthValidationError.NAME_TOO_LONG
        else -> null
    }

    fun validatePhone(phone: String): AuthValidationError? =
        OrderValidation.validatePhone(phone)?.let { AuthValidationError.PHONE_INVALID }

    fun validatePassword(password: String): AuthValidationError? = when {
        password.length < PASSWORD_MIN_LENGTH -> AuthValidationError.PASSWORD_TOO_SHORT
        password.length > PASSWORD_MAX_LENGTH -> AuthValidationError.PASSWORD_TOO_LONG
        else -> null
    }

    fun validate(request: RegisterRequest): List<AuthValidationError> = listOfNotNull(
        validateName(request.name),
        validatePhone(request.phone),
        validatePassword(request.password),
    )
}
