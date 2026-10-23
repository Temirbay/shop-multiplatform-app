package kz.mtemirbay.shop.core.data

import com.russhwolf.settings.Settings
import kz.mtemirbay.shop.core.data.cart.CartRepository
import kz.mtemirbay.shop.core.data.cart.CartRepositoryImpl
import kz.mtemirbay.shop.core.data.cart.SettingsCartStorage
import kz.mtemirbay.shop.core.data.session.SessionRepository
import kz.mtemirbay.shop.core.data.session.SettingsSessionRepository
import kz.mtemirbay.shop.core.network.AuthTokenProvider
import org.koin.dsl.binds
import org.koin.dsl.module

val dataModule = module {
    // single — одна корзина на всё приложение: каталог, товар и корзина видят одни и те же данные
    single<CartRepository> { CartRepositoryImpl(SettingsCartStorage(Settings())) }
    // Один объект под двумя интерфейсами: фичи видят SessionRepository, а сеть — только AuthTokenProvider
    single { SettingsSessionRepository(Settings()) } binds arrayOf(SessionRepository::class, AuthTokenProvider::class)
}
