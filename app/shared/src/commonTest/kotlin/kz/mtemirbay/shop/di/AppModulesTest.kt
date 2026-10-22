package kz.mtemirbay.shop.di

import io.ktor.client.HttpClient
import kz.mtemirbay.shop.core.data.cart.CartRepository
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class AppModulesTest {

    @Test
    fun dependencyGraphResolves() {
        // koinApplication, а не startKoin: не трогает глобальный Koin и не мешает другим тестам
        val koin = koinApplication { modules(appModules) }.koin

        assertNotNull(koin.get<HttpClient>())
        // single: все фичи получают один и тот же экземпляр корзины
        assertSame(koin.get<CartRepository>(), koin.get<CartRepository>())
    }
}
