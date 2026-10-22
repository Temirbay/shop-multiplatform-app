package kz.mtemirbay.shop.di

import io.ktor.client.HttpClient
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModulesTest {

    @Test
    fun dependencyGraphResolves() {
        // koinApplication, а не startKoin: не трогает глобальный Koin и не мешает другим тестам
        val koin = koinApplication { modules(appModules) }.koin

        assertNotNull(koin.get<HttpClient>())
    }
}
