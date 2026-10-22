package kz.mtemirbay.shop.core.data.cart

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryCartRepositoryTest {

    private val repository = InMemoryCartRepository()

    @Test
    fun addingSameProductIncreasesQuantity() = runTest {
        repository.addPhone()
        repository.addPhone()

        assertEquals(listOf(phone.copy(quantity = 2)), repository.items.value)
    }

    @Test
    fun differentProductsAreSeparateItemsInAddOrder() = runTest {
        repository.addPhone()
        repository.add(productId = 2, title = "Книга", priceKzt = 10_000, imageUrl = "")

        assertEquals(listOf(1L, 2L), repository.items.value.map { it.productId })
    }

    @Test
    fun setQuantityToZeroRemovesItem() = runTest {
        repository.addPhone()

        repository.setQuantity(productId = 1, quantity = 0)

        assertEquals(emptyList(), repository.items.value)
    }

    @Test
    fun setQuantityChangesOnlyThatItem() = runTest {
        repository.addPhone()
        repository.add(productId = 2, title = "Книга", priceKzt = 10_000, imageUrl = "")

        repository.setQuantity(productId = 2, quantity = 5)

        assertEquals(listOf(1, 5), repository.items.value.map { it.quantity })
    }

    @Test
    fun clearEmptiesCart() = runTest {
        repository.addPhone()

        repository.clear()

        assertEquals(emptyList(), repository.items.value)
    }

    private val phone = CartItem(productId = 1, title = "Смартфон", priceKzt = 189_990, imageUrl = "", quantity = 1)

    private suspend fun CartRepository.addPhone() =
        add(phone.productId, phone.title, phone.priceKzt, phone.imageUrl)
}
