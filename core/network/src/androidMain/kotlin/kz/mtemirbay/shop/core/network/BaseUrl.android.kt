package kz.mtemirbay.shop.core.network

// Не 10.0.2.2: с Android 17 (targetSdk 37) приложению нужно разрешение ACCESS_LOCAL_NETWORK для адресов
// локальной сети. localhost — loopback, разрешение не нужно. До компьютера его пробрасывает
// `adb reverse tcp:8080 tcp:8080` (задача adbReverse в :app:androidApp) — так работает и эмулятор, и телефон по USB
internal actual val defaultBaseUrl: String = "http://localhost:8080/"
