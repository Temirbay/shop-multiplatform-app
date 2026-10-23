package kz.mtemirbay.shop.di

import kz.mtemirbay.shop.core.data.dataModule
import kz.mtemirbay.shop.core.network.networkModule
import kz.mtemirbay.shop.feature.account.di.accountModule
import kz.mtemirbay.shop.feature.cart.di.cartModule
import kz.mtemirbay.shop.feature.catalog.di.catalogModule
import kz.mtemirbay.shop.feature.checkout.di.checkoutModule
import kz.mtemirbay.shop.feature.product.di.productModule
import org.koin.core.context.startKoin
import kz.mtemirbay.shop.AppViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration

/** Все Koin-модули приложения. Новый модуль (например, catalogModule) добавляется сюда. */
private val appModule = module {
    viewModelOf(::AppViewModel)
}

val appModules: List<Module> = listOf(
    appModule,
    networkModule,
    dataModule,
    catalogModule,
    productModule,
    cartModule,
    checkoutModule,
    accountModule,
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
