package kz.mtemirbay.shop.feature.account.data

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.core.data.session.SessionRepository
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.AuthResponse
import kz.mtemirbay.shop.core.model.LoginRequest
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.core.model.RegisterRequest
import kz.mtemirbay.shop.core.network.safeApiCall
import kz.mtemirbay.shop.feature.account.domain.AuthRejectedException
import kz.mtemirbay.shop.feature.account.domain.AuthRepository

internal class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override suspend fun login(phone: String, password: String): Result<Unit> = authenticate {
        api.login(LoginRequest(OrderValidation.normalizePhone(phone), password))
    }

    override suspend fun register(name: String, phone: String, password: String): Result<Unit> = authenticate {
        api.register(RegisterRequest(name.trim(), OrderValidation.normalizePhone(phone), password))
    }

    private suspend fun authenticate(call: suspend () -> AuthResponse): Result<Unit> {
        val result = safeApiCall(call)
        result.onSuccess { response ->
            sessionRepository.save(
                Session(token = response.token, userId = response.user.id, name = response.user.name, phone = response.user.phone),
            )
        }
        val error = result.exceptionOrNull()
        if (error is ClientRequestException) {
            // 400/401/409 с ApiError — отказ сервера с понятной причиной
            val apiError = safeApiCall { error.response.body<ApiError>() }.getOrNull()
            if (apiError != null) return Result.failure(AuthRejectedException(apiError.code))
        }
        return result.map { }
    }
}
