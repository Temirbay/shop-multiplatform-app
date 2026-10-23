package kz.mtemirbay.shop.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kz.mtemirbay.shop.auth.JwtConfig
import kz.mtemirbay.shop.auth.PasswordHasher
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import kz.mtemirbay.shop.core.model.AuthResponse
import kz.mtemirbay.shop.core.model.AuthValidation
import kz.mtemirbay.shop.core.model.LoginRequest
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.core.model.RegisterRequest
import kz.mtemirbay.shop.core.model.UserDto
import kz.mtemirbay.shop.data.User
import kz.mtemirbay.shop.data.UserRepository

fun Route.authRoutes(users: UserRepository, hasher: PasswordHasher, jwt: JwtConfig) {
    post("/auth/register") {
        val request = call.receive<RegisterRequest>()
        AuthValidation.validate(request).firstOrNull()?.let { error ->
            return@post call.respond(HttpStatusCode.BadRequest, ApiError(error.name, "Validation failed: $error"))
        }
        val user = users.create(
            name = request.name.trim(),
            phone = OrderValidation.normalizePhone(request.phone),
            passwordHash = hasher.hash(request.password),
        ) ?: return@post call.respond(
            HttpStatusCode.Conflict,
            ApiError(AuthErrorCodes.PHONE_TAKEN, "Phone already registered"),
        )
        call.respond(HttpStatusCode.Created, AuthResponse(jwt.createToken(user.id), user.toDto()))
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()
        val user = users.findByPhone(OrderValidation.normalizePhone(request.phone))
        if (user == null || !hasher.verify(request.password, user.passwordHash)) {
            return@post call.respondInvalidCredentials()
        }
        call.respond(AuthResponse(jwt.createToken(user.id), user.toDto()))
    }

    authenticate(JwtConfig.AUTH_NAME) {
        get("/me") {
            val user = users.findById(call.userId()) ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ApiError(AuthErrorCodes.UNAUTHORIZED, "User not found"),
            )
            call.respond(user.toDto())
        }
    }
}

/** id пользователя из проверенного токена. Только внутри authenticate { } */
fun RoutingCall.userId(): Long = principal<JWTPrincipal>()!!.payload.getClaim(JwtConfig.USER_ID_CLAIM).asLong()

private suspend fun RoutingCall.respondInvalidCredentials() {
    respond(HttpStatusCode.Unauthorized, ApiError(AuthErrorCodes.INVALID_CREDENTIALS, "Invalid phone or password"))
}

private fun User.toDto() = UserDto(id = id, name = name, phone = phone)
