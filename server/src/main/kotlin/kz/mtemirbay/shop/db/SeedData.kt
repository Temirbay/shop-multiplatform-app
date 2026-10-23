package kz.mtemirbay.shop.db

import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto

/** Начальные данные: записываются в пустую БД при первом старте */
internal object SeedData {

    val categories = listOf(
        CategoryDto(id = 1, title = "Электроника"),
        CategoryDto(id = 2, title = "Одежда"),
        CategoryDto(id = 3, title = "Книги"),
        CategoryDto(id = 4, title = "Дом"),
    )

    val products = listOf(
        product(1, "Смартфон Nova X", "6.5\" OLED, 128 ГБ, 5000 мА·ч", 189_990, categoryId = 1),
        product(2, "Беспроводные наушники Pulse", "Шумоподавление, до 30 часов работы", 39_990, categoryId = 1),
        product(3, "Ноутбук Air 14", "14\", 16 ГБ RAM, 512 ГБ SSD", 449_990, categoryId = 1),
        product(4, "Умные часы Fit 3", "Пульс, SpO2, GPS, водозащита 5 ATM", 59_990, categoryId = 1),
        product(5, "Портативная колонка Boom", "20 Вт, Bluetooth 5.3, IPX7", 24_990, categoryId = 1),
        product(6, "Пуховик городской", "Тёплый, до −25 °C, капюшон", 69_990, categoryId = 2),
        product(7, "Кроссовки Runner", "Лёгкие беговые кроссовки", 34_990, categoryId = 2),
        product(8, "Худи Basic", "Хлопок 100%, оверсайз", 14_990, categoryId = 2),
        product(9, "Джинсы Straight", "Прямой крой, плотный деним", 19_990, categoryId = 2),
        product(10, "Шапка вязаная", "Шерсть мериноса", 6_990, categoryId = 2),
        product(11, "Kotlin in Action", "Книга о языке Kotlin от разработчиков JetBrains", 12_990, categoryId = 3),
        product(12, "Clean Architecture", "Роберт Мартин о структуре программ", 10_990, categoryId = 3),
        product(13, "Абай. Слова назидания", "Классика казахской литературы", 4_990, categoryId = 3),
        product(14, "Designing Data-Intensive Applications", "Мартин Клеппман о системах данных", 15_990, categoryId = 3),
        product(15, "Кофемашина Barista", "Рожковая, 15 бар, капучинатор", 129_990, categoryId = 4),
        product(16, "Робот-пылесос Clean S", "Влажная уборка, карта помещений", 99_990, categoryId = 4),
        product(17, "Настольная лампа Lumen", "Регулировка яркости и температуры", 11_990, categoryId = 4),
        product(18, "Набор посуды Chef", "10 предметов, нержавеющая сталь", 44_990, categoryId = 4),
    )

    private fun product(id: Long, title: String, description: String, priceKzt: Long, categoryId: Long) =
        ProductDto(
            id = id,
            title = title,
            description = description,
            priceKzt = priceKzt,
            imageUrl = "https://picsum.photos/seed/shop-$id/600/600",
            categoryId = categoryId,
        )
}
