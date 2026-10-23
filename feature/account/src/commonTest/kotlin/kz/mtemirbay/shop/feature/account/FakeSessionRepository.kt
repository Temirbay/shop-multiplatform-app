package kz.mtemirbay.shop.feature.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.core.data.session.SessionRepository

internal class FakeSessionRepository(initial: Session? = null) : SessionRepository {
    private val _session = MutableStateFlow(initial)
    override val session: StateFlow<Session?> = _session

    override suspend fun save(session: Session) {
        _session.value = session
    }

    override suspend fun clear() {
        _session.value = null
    }
}
