package kz.mtemirbay.shop.feature.checkout.di

import kz.mtemirbay.shop.feature.checkout.data.CheckoutRepositoryImpl
import kz.mtemirbay.shop.feature.checkout.data.OrderApi
import kz.mtemirbay.shop.feature.checkout.domain.CheckoutRepository
import kz.mtemirbay.shop.feature.checkout.presentation.CheckoutViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val checkoutModule = module {
    singleOf(::OrderApi)
    singleOf(::CheckoutRepositoryImpl) bind CheckoutRepository::class
    viewModelOf(::CheckoutViewModel)
}
