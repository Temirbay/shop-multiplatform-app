package kz.mtemirbay.shop

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import kz.mtemirbay.shop.auth.JwtConfig
import kz.mtemirbay.shop.auth.PasswordHasher
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.AuthErrorCodes
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kz.mtemirbay.shop.data.OrderRepository
import kz.mtemirbay.shop.data.ProductRepository
import kz.mtemirbay.shop.data.UserRepository
import kz.mtemirbay.shop.di.serverModule
import kz.mtemirbay.shop.routes.authRoutes
import kz.mtemirbay.shop.routes.categoryRoutes
import kz.mtemirbay.shop.routes.orderRoutes
import kz.mtemirbay.shop.routes.productRoutes
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/** Порт, URL базы и список модулей — в application.conf */
fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val jwtConfig = JwtConfig.from(environment.config)
    install(Koin) {
        slf4jLogger()
        modules(serverModule(databaseUrl = environment.config.property("database.url").getString(), jwtConfig = jwtConfig))
    }
    install(Authentication) {
        jwt(JwtConfig.AUTH_NAME) {
            verifier(jwtConfig.verifier)
            validate { credential ->
                // Подпись и срок уже проверил verifier; здесь — что в токене есть id пользователя
                credential.payload.getClaim(JwtConfig.USER_ID_CLAIM).asLong()?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiError(AuthErrorCodes.UNAUTHORIZED, "Token is missing or invalid"))
            }
        }
    }

    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
    install(CORS) {
        // Только для разработки: веб-версия запускается на другом порту, и без CORS браузер блокирует запросы
        anyHost()
        // Для POST/PUT с JSON-телом (корзина) браузер спросит разрешение на этот заголовок
        allowHeader(HttpHeaders.ContentType)
    }
    install(StatusPages) {
        // Из нескольких обработчиков выбирается самый специфичный по иерархии классов
        exception<NotFoundException> { call, cause ->
            call.respondText(cause.message.orEmpty(), status = HttpStatusCode.NotFound)
        }
        exception<BadRequestException> { call, cause ->
            call.respondText(cause.message.orEmpty(), status = HttpStatusCode.BadRequest)
        }
        // Всё остальное — 500 без stacktrace наружу
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respondText("Internal server error", status = HttpStatusCode.InternalServerError)
        }
    }
    val productRepository by inject<ProductRepository>()
    val orderRepository by inject<OrderRepository>()
    val userRepository by inject<UserRepository>()
    val passwordHasher by inject<PasswordHasher>()
    routing {
        authRoutes(userRepository, passwordHasher, jwtConfig)
        productRoutes(productRepository)
        categoryRoutes(productRepository)
        orderRoutes(productRepository, orderRepository)
    }
}
