package kz.mtemirbay.shop.core.data.session

import kotlinx.coroutines.flow.StateFlow

data class Session(
    val token: String,
    val userId: Long,
    val name: String,
    /** +7XXXXXXXXXX */
    val phone: String,
)

/** Кто вошёл в приложение. null — гость */
interface SessionRepository {
    val session: StateFlow<Session?>

    suspend fun save(session: Session)

    suspend fun clear()
}
