package kz.mtemirbay.shop.feature.account.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kz.mtemirbay.shop.core.model.AuthResponse
import kz.mtemirbay.shop.core.model.LoginRequest
import kz.mtemirbay.shop.core.model.RegisterRequest

internal class AuthApi(private val client: HttpClient) {

    suspend fun register(request: RegisterRequest): AuthResponse =
        client.post("auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
