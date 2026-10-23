package kz.mtemirbay.shop.core.data.session

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsSessionRepositoryTest {

    private val settings = MapSettings()
    private val session = Session(token = "jwt", userId = 1, name = "Мирас", phone = "+77011234567")

    @Test
    fun sessionSurvivesRestart() = runTest {
        SettingsSessionRepository(settings).save(session)

        val afterRestart = SettingsSessionRepository(settings)

        assertEquals(session, afterRestart.session.value)
        assertEquals("jwt", afterRestart.currentToken())
    }

    @Test
    fun clearLogsOutAndForgetsToken() = runTest {
        val repository = SettingsSessionRepository(settings)
        repository.save(session)

        repository.clear()

        assertNull(repository.session.value)
        assertNull(SettingsSessionRepository(settings).session.value)
    }

    @Test
    fun unauthorizedResponseLogsOut() = runTest {
        val repository = SettingsSessionRepository(settings)
        repository.save(session)

        repository.onUnauthorized()

        assertNull(repository.currentToken())
    }
}
