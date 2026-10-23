package kz.mtemirbay.shop.feature.account.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.core.data.session.SessionRepository
import kz.mtemirbay.shop.core.ui.MviViewModel

internal sealed interface ProfileState {
    data object Loading : ProfileState
    data object Guest : ProfileState
    data class LoggedIn(val name: String, val phone: String) : ProfileState
}

internal sealed interface ProfileEvent {
    data object Logout : ProfileEvent
}

internal class ProfileViewModel(
    private val sessionRepository: SessionRepository,
) : MviViewModel<ProfileState, ProfileEvent, Nothing>(ProfileState.Loading) {

    init {
        sessionRepository.session
            .onEach { session -> setState { session.toState() } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Logout -> viewModelScope.launch { sessionRepository.clear() }
        }
    }

    private fun Session?.toState() = if (this == null) ProfileState.Guest else ProfileState.LoggedIn(name, phone)
}
