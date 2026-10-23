package kz.mtemirbay.shop.feature.orders.di

import kz.mtemirbay.shop.feature.orders.data.OrdersApi
import kz.mtemirbay.shop.feature.orders.data.OrdersRepositoryImpl
import kz.mtemirbay.shop.feature.orders.domain.OrdersRepository
import kz.mtemirbay.shop.feature.orders.presentation.MyOrdersViewModel
import kz.mtemirbay.shop.feature.orders.presentation.OrderDetailsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val ordersModule = module {
    singleOf(::OrdersApi)
    singleOf(::OrdersRepositoryImpl) bind OrdersRepository::class
    viewModelOf(::MyOrdersViewModel)
    viewModel { params -> OrderDetailsViewModel(orderId = params.get(), repository = get()) }
}
