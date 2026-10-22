package kz.mtemirbay.shop.feature.catalog.di

import kz.mtemirbay.shop.feature.catalog.data.CatalogApi
import kz.mtemirbay.shop.feature.catalog.data.CatalogRepositoryImpl
import kz.mtemirbay.shop.feature.catalog.domain.CatalogRepository
import kz.mtemirbay.shop.feature.catalog.presentation.CatalogViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val catalogModule = module {
    singleOf(::CatalogApi)
    singleOf(::CatalogRepositoryImpl) bind CatalogRepository::class
    // viewModelOf — ViewModel живёт, пока жив экран, и переживает поворот
    viewModelOf(::CatalogViewModel)
}
