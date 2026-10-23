package kz.mtemirbay.shop.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    /** Нормализованный: +7XXXXXXXXXX */
    val phone: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val phone: String,
)

/** Коды ошибок авторизации в [ApiError.code] */
object AuthErrorCodes {
    /** 409 при регистрации */
    const val PHONE_TAKEN = "PHONE_TAKEN"

    /** 401 при входе. Один код и для «нет такого телефона», и для «неверный пароль» — чтобы нельзя было перебором узнать, чьи номера зарегистрированы */
    const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"

    /** 401 на защищённом роуте: нет токена или он истёк */
    const val UNAUTHORIZED = "UNAUTHORIZED"
}
