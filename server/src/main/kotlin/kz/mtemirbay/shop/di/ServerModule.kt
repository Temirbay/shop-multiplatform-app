package kz.mtemirbay.shop.di

import kz.mtemirbay.shop.auth.JwtConfig
import kz.mtemirbay.shop.auth.PasswordHasher
import kz.mtemirbay.shop.data.ExposedOrderRepository
import kz.mtemirbay.shop.data.ExposedProductRepository
import kz.mtemirbay.shop.data.ExposedUserRepository
import kz.mtemirbay.shop.data.OrderRepository
import kz.mtemirbay.shop.data.ProductRepository
import kz.mtemirbay.shop.data.UserRepository
import kz.mtemirbay.shop.db.DatabaseFactory
import org.koin.dsl.module

fun serverModule(databaseUrl: String, jwtConfig: JwtConfig) = module {
    single { DatabaseFactory.init(databaseUrl) }
    single<ProductRepository> { ExposedProductRepository(get()) }
    single<OrderRepository> { ExposedOrderRepository(get()) }
    single<UserRepository> { ExposedUserRepository(get()) }
    single { PasswordHasher() }
    single { jwtConfig }
}
