package kz.mtemirbay.shop.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date
import kotlin.time.Duration.Companion.days

class JwtConfig(
    secret: String,
    private val issuer: String,
    private val audience: String,
    private val validityDays: Int,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    /** В токене только id пользователя: имя и телефон берутся из БД, иначе после их смены токен врал бы */
    fun createToken(userId: Long): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim(USER_ID_CLAIM, userId)
        .withExpiresAt(Date(System.currentTimeMillis() + validityDays.days.inWholeMilliseconds))
        .sign(algorithm)

    companion object {
        const val USER_ID_CLAIM = "userId"
        const val AUTH_NAME = "jwt"

        fun from(config: ApplicationConfig) = JwtConfig(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            validityDays = config.property("jwt.validityDays").getString().toInt(),
        )
    }
}
