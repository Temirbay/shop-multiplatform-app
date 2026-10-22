package kz.mtemirbay.shop.core.data.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Логика корзины. Где она хранится — решает [storage]: на диске в приложении, в памяти в тестах */
class CartRepositoryImpl(private val storage: CartStorage) : CartRepository {

    private val mutex = Mutex()

    private val _items = MutableStateFlow(storage.load())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    override suspend fun add(productId: Long, title: String, priceKzt: Long, imageUrl: String) = mutate { items ->
        if (items.any { it.productId == productId }) {
            items.map { if (it.productId == productId) it.copy(quantity = it.quantity + 1) else it }
        } else {
            items + CartItem(productId, title, priceKzt, imageUrl, quantity = 1)
        }
    }

    override suspend fun setQuantity(productId: Long, quantity: Int) = mutate { items ->
        if (quantity <= 0) {
            items.filterNot { it.productId == productId }
        } else {
            items.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
    }

    override suspend fun remove(productId: Long) = mutate { items ->
        items.filterNot { it.productId == productId }
    }

    override suspend fun clear() = mutate { emptyList() }

    /**
     * Изменение и запись — под одним Mutex. Иначе при двух быстрых нажатиях «+» корутины могут сохранить
     * на диск состояния в обратном порядке, и после перезапуска окажется старое количество
     */
    private suspend fun mutate(change: (List<CartItem>) -> List<CartItem>) {
        mutex.withLock {
            _items.update(change)
            storage.save(_items.value)
        }
    }
}
