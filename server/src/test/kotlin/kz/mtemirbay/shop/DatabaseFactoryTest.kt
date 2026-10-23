package kz.mtemirbay.shop

import kotlinx.coroutines.test.runTest
import kz.mtemirbay.shop.data.ExposedProductRepository
import kz.mtemirbay.shop.db.DatabaseFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseFactoryTest {

    @Test
    fun seedIsNotDuplicatedOnRestart() = runTest {
        val url = inMemoryDatabaseUrl()

        DatabaseFactory.init(url)
        // «Перезапуск сервера» на той же БД
        val database = DatabaseFactory.init(url)

        val repository = ExposedProductRepository(database)
        assertEquals(18, repository.getProducts().size)
        assertEquals(4, repository.getCategories().size)
    }
}
