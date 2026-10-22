package kz.mtemirbay.shop.core.network

/** Адрес локального :server. С Android-эмулятора localhost указывает на сам эмулятор, поэтому адрес у каждой платформы свой. */
internal expect val defaultBaseUrl: String
