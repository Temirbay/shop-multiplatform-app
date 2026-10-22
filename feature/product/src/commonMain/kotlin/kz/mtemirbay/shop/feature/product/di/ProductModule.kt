package kz.mtemirbay.shop.feature.product.di

import kz.mtemirbay.shop.feature.product.data.ProductApi
import kz.mtemirbay.shop.feature.product.data.ProductRepositoryImpl
import kz.mtemirbay.shop.feature.product.domain.ProductRepository
import kz.mtemirbay.shop.feature.product.presentation.ProductViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val productModule = module {
    singleOf(::ProductApi)
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    // params.get() — productId, переданный из экрана через parametersOf
    viewModel { params -> ProductViewModel(productId = params.get(), repository = get(), cartRepository = get()) }
}
