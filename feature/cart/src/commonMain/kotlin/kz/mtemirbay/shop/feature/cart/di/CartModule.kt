package kz.mtemirbay.shop.feature.cart.di

import kz.mtemirbay.shop.feature.cart.presentation.CartViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
    viewModelOf(::CartViewModel)
}
