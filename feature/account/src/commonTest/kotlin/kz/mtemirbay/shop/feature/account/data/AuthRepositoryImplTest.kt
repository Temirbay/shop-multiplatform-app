package kz.mtemirbay.shop.feature.account.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import kz.mtemirbay.shop.core.model.LoginRequest
import kz.mtemirbay.shop.core.network.createHttpClient
import kz.mtemirbay.shop.feature.account.FakeSessionRepository
import kz.mtemirbay.shop.feature.account.domain.AuthRejectedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthRepositoryImplTest {

    private val sessionRepository = FakeSessionRepository()
    private var sentBody = ""

    @Test
    fun successfulLoginSavesSession() = runTest {
        val repository = repositoryRespondingWith(
            HttpStatusCode.OK,
            """{"token":"jwt","user":{"id":7,"name":"Мирас","phone":"+77011234567"}}""",
        )

        repository.login("+7 (701) 123-45-67", "password1").getOrThrow()

        assertEquals(LoginRequest("+77011234567", "password1"), Json.decodeFromString<LoginRequest>(sentBody))
        assertEquals(Session("jwt", 7, "Мирас", "+77011234567"), sessionRepository.session.value)
    }

    @Test
    fun wrongPasswordIsRejectedAndSessionNotSaved() = runTest {
        val repository = repositoryRespondingWith(
            HttpStatusCode.Unauthorized,
            """{"code":"${AuthErrorCodes.INVALID_CREDENTIALS}","message":"x"}""",
        )

        val error = repository.login("+77011234567", "wrong").exceptionOrNull()

        assertIs<AuthRejectedException>(error)
        assertEquals(AuthErrorCodes.INVALID_CREDENTIALS, error.code)
        assertNull(sessionRepository.session.value)
    }

    @Test
    fun takenPhoneOnRegisterIsRejected() = runTest {
        val repository = repositoryRespondingWith(
            HttpStatusCode.Conflict,
            """{"code":"${AuthErrorCodes.PHONE_TAKEN}","message":"x"}""",
        )

        val error = repository.register("Мирас", "+77011234567", "password1").exceptionOrNull()

        assertEquals(AuthErrorCodes.PHONE_TAKEN, (error as AuthRejectedException).code)
    }

    private fun repositoryRespondingWith(status: HttpStatusCode, body: String) = AuthRepositoryImpl(
        AuthApi(
            createHttpClient(
                baseUrl = "http://test.local/",
                engine = MockEngine { request ->
                    sentBody = request.body.toByteArray().decodeToString()
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
            ),
        ),
        sessionRepository,
    )
}
