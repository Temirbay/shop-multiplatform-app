package kz.mtemirbay.shop.core.ui

/** 189990 → "189 990 ₸". Пробелы неразрывные, чтобы цена не переносилась на две строки. */
fun formatPriceKzt(priceKzt: Long): String {
    val digits = priceKzt.toString()
    val grouped = digits.reversed().chunked(3).joinToString(" ").reversed()
    return "$grouped ₸"
}
