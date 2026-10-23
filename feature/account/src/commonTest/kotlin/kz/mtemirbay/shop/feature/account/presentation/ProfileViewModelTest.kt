package kz.mtemirbay.shop.feature.account.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.feature.account.FakeSessionRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun followsSessionAndLogsOut() = runTest {
        val sessionRepository = FakeSessionRepository()
        val viewModel = ProfileViewModel(sessionRepository)
        advanceUntilIdle()
        assertEquals(ProfileState.Guest, viewModel.state.value)

        // Вход произошёл на форме — профиль узнаёт об этом через сессию
        sessionRepository.save(Session("jwt", 1, "Мирас", "+77011234567"))
        advanceUntilIdle()
        assertEquals(ProfileState.LoggedIn("Мирас", "+77011234567"), viewModel.state.value)

        viewModel.onEvent(ProfileEvent.Logout)
        advanceUntilIdle()
        assertEquals(ProfileState.Guest, viewModel.state.value)
    }
}
