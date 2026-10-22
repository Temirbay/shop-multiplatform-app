package kz.mtemirbay.shop.core.data.cart

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsCartStorageTest {

    // MapSettings — Settings в памяти: тот же API, что у SharedPreferences/localStorage, но без платформы
    private val settings = MapSettings()

    @Test
    fun cartSurvivesRestart() = runTest {
        val beforeRestart = CartRepositoryImpl(SettingsCartStorage(settings))
        beforeRestart.add(1, "Смартфон", 189_990, "img")
        beforeRestart.add(1, "Смартфон", 189_990, "img")
        beforeRestart.add(11, "Книга", 12_990, "img")

        // «Перезапуск»: новый репозиторий на том же хранилище
        val afterRestart = CartRepositoryImpl(SettingsCartStorage(settings))

        assertEquals(
            listOf(
                CartItem(1, "Смартфон", 189_990, "img", quantity = 2),
                CartItem(11, "Книга", 12_990, "img", quantity = 1),
            ),
            afterRestart.items.value,
        )
    }

    @Test
    fun removalIsAlsoPersisted() = runTest {
        val beforeRestart = CartRepositoryImpl(SettingsCartStorage(settings))
        beforeRestart.add(1, "Смартфон", 189_990, "img")
        beforeRestart.remove(1)

        assertEquals(emptyList(), SettingsCartStorage(settings).load())
    }

    @Test
    fun emptyStorageGivesEmptyCart() {
        assertEquals(emptyList(), SettingsCartStorage(settings).load())
    }

    @Test
    fun corruptedDataGivesEmptyCartInsteadOfCrash() {
        settings.putString("cart_v1", "{not a json")

        assertEquals(emptyList(), SettingsCartStorage(settings).load())
    }
}
