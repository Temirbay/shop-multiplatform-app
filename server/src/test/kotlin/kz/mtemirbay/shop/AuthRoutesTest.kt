package kz.mtemirbay.shop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import kz.mtemirbay.shop.core.model.AuthResponse
import kz.mtemirbay.shop.core.model.AuthValidationError
import kz.mtemirbay.shop.core.model.LoginRequest
import kz.mtemirbay.shop.core.model.RegisterRequest
import kz.mtemirbay.shop.core.model.UserDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    private val registration = RegisterRequest(name = "Мирас", phone = "+7 (701) 123-45-67", password = "password1")

    @Test
    fun `register returns token and normalized user`() = shopTestApplication { client ->
        val response = client.register(registration)

        assertEquals(HttpStatusCode.Created, response.status)
        val auth = response.body<AuthResponse>()
        assertTrue(auth.token.isNotBlank())
        assertEquals(UserDto(id = 1, name = "Мирас", phone = "+77011234567"), auth.user)
    }

    @Test
    fun `same phone cannot register twice`() = shopTestApplication { client ->
        client.register(registration)

        val response = client.register(registration.copy(name = "Другой", phone = "+77011234567"))

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(AuthErrorCodes.PHONE_TAKEN, response.body<ApiError>().code)
    }

    @Test
    fun `short password is rejected`() = shopTestApplication { client ->
        val response = client.register(registration.copy(password = "1234567"))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(AuthValidationError.PASSWORD_TOO_SHORT.name, response.body<ApiError>().code)
    }

    @Test
    fun `login with correct password returns token`() = shopTestApplication { client ->
        client.register(registration)

        val response = client.login(LoginRequest(phone = "+77011234567", password = "password1"))

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Мирас", response.body<AuthResponse>().user.name)
    }

    @Test
    fun `wrong password and unknown phone give the same error`() = shopTestApplication { client ->
        client.register(registration)

        val wrongPassword = client.login(LoginRequest(phone = "+77011234567", password = "wrong-password"))
        val unknownPhone = client.login(LoginRequest(phone = "+77000000000", password = "password1"))

        assertEquals(HttpStatusCode.Unauthorized, wrongPassword.status)
        assertEquals(HttpStatusCode.Unauthorized, unknownPhone.status)
        assertEquals(AuthErrorCodes.INVALID_CREDENTIALS, wrongPassword.body<ApiError>().code)
        assertEquals(AuthErrorCodes.INVALID_CREDENTIALS, unknownPhone.body<ApiError>().code)
    }

    @Test
    fun `me returns current user by token`() = shopTestApplication { client ->
        val token = client.register(registration).body<AuthResponse>().token

        val me = client.get("/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, me.status)
        assertEquals("+77011234567", me.body<UserDto>().phone)
    }

    @Test
    fun `me without token or with forged token is 401`() = shopTestApplication { client ->
        val noToken = client.get("/me")
        val forged = client.get("/me") { bearerAuth("not.a.jwt") }

        assertEquals(HttpStatusCode.Unauthorized, noToken.status)
        assertEquals(HttpStatusCode.Unauthorized, forged.status)
        assertEquals(AuthErrorCodes.UNAUTHORIZED, noToken.body<ApiError>().code)
    }

    private suspend fun HttpClient.register(request: RegisterRequest) = post("/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    private suspend fun HttpClient.login(request: LoginRequest) = post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
