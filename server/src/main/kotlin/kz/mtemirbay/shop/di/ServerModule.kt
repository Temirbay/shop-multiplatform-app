package kz.mtemirbay.shop.di

import kz.mtemirbay.shop.data.ExposedOrderRepository
import kz.mtemirbay.shop.data.ExposedProductRepository
import kz.mtemirbay.shop.data.OrderRepository
import kz.mtemirbay.shop.data.ProductRepository
import kz.mtemirbay.shop.db.DatabaseFactory
import org.koin.dsl.module

fun serverModule(databaseUrl: String) = module {
    single { DatabaseFactory.init(databaseUrl) }
    single<ProductRepository> { ExposedProductRepository(get()) }
    single<OrderRepository> { ExposedOrderRepository(get()) }
}
