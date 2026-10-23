package kz.mtemirbay.shop.feature.account.di

import kz.mtemirbay.shop.feature.account.data.AuthApi
import kz.mtemirbay.shop.feature.account.data.AuthRepositoryImpl
import kz.mtemirbay.shop.feature.account.domain.AuthRepository
import kz.mtemirbay.shop.feature.account.presentation.AuthViewModel
import kz.mtemirbay.shop.feature.account.presentation.ProfileViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val accountModule = module {
    singleOf(::AuthApi)
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
}
