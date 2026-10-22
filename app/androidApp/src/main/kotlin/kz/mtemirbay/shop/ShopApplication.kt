package kz.mtemirbay.shop

import android.app.Application
import kz.mtemirbay.shop.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ShopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@ShopApplication)
        }
    }
}
