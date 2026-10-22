package kz.mtemirbay.shop.di

import kz.mtemirbay.shop.core.network.networkModule
import kz.mtemirbay.shop.feature.catalog.di.catalogModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Все Koin-модули приложения. Новый модуль (например, catalogModule) добавляется сюда. */
val appModules: List<Module> = listOf(
    networkModule,
    catalogModule,
)

/**
 * Запускается один раз из точки входа каждой платформы: ShopApplication (Android), main() (Desktop, Web),
 * iOSApp.init (iOS). config — для платформенной настройки, например androidContext() на Android.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
