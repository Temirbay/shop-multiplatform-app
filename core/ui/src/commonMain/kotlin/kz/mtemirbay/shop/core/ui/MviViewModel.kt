package kz.mtemirbay.shop.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Однонаправленный поток данных: UI шлёт [onEvent] → ViewModel меняет [state] → UI перерисовывается.
 *
 * @param S состояние экрана — всё, что нужно для отрисовки
 * @param E события от UI: клики, ввод текста
 * @param F одноразовые эффекты: навигация, снекбар. Не state, потому что их нельзя «показать повторно» после поворота экрана
 */
abstract class MviViewModel<S, E, F>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<F>(Channel.BUFFERED)
    val effects: Flow<F> = _effects.receiveAsFlow()

    abstract fun onEvent(event: E)

    protected fun setState(reduce: S.() -> S) {
        _state.update(reduce)
    }

    protected fun sendEffect(effect: F) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
