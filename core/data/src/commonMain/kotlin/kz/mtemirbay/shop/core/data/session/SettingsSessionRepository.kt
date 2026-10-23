package kz.mtemirbay.shop.core.data.session

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kz.mtemirbay.shop.core.network.AuthTokenProvider

/**
 * Сессия в Settings — переживает перезапуск, как корзина.
 *
 * ⚠️ Settings — НЕ защищённое хранилище: на рутованном телефоне токен можно прочитать.
 * В продакшене токен кладут в Keychain (iOS) / EncryptedSharedPreferences или Keystore (Android).
 * Для учебного проекта — осознанное упрощение
 */
class SettingsSessionRepository(private val settings: Settings) : SessionRepository, AuthTokenProvider {

    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(load())
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun save(session: Session) {
        settings.putString(KEY, json.encodeToString(StoredSession.from(session)))
        _session.value = session
    }

    override suspend fun clear() = clearNow()

    override fun currentToken(): String? = _session.value?.token

    override fun onUnauthorized() = clearNow()

    private fun clearNow() {
        settings.remove(KEY)
        _session.value = null
    }

    private fun load(): Session? {
        val stored = settings.getStringOrNull(KEY) ?: return null
        return try {
            json.decodeFromString<StoredSession>(stored).toSession()
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        const val KEY = "session_v1"
    }
}

@Serializable
private data class StoredSession(val token: String, val userId: Long, val name: String, val phone: String) {
    fun toSession() = Session(token, userId, name, phone)

    companion object {
        fun from(session: Session) = StoredSession(session.token, session.userId, session.name, session.phone)
    }
}
