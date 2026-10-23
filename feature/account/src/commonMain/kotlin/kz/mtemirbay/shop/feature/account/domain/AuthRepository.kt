package kz.mtemirbay.shop.feature.account.domain

internal interface AuthRepository {
    /** Успех — сессия сохранена. Ошибки: [AuthRejectedException] (сервер отказал) или сетевая */
    suspend fun login(phone: String, password: String): Result<Unit>

    suspend fun register(name: String, phone: String, password: String): Result<Unit>
}

/** @param code — [kz.mtemirbay.shop.core.model.ApiError.code]: INVALID_CREDENTIALS, PHONE_TAKEN или ошибка валидации */
internal class AuthRejectedException(val code: String) : Exception("Auth rejected: $code")
