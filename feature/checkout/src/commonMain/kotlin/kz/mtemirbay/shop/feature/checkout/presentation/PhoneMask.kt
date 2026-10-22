package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Номер без кода страны: 10 цифр. Код +7 — фиксированный префикс поля */
internal const val PHONE_DIGITS = 10

internal const val COUNTRY_CODE = "+7"

/**
 * Из того, что ввёл или вставил пользователь, — только цифры номера, не больше 10.
 * Вставка «+7 701 123 45 67» или «8 701 123 45 67» (11 цифр за раз) — первая 7/8 это код страны, отбрасываем.
 * Ввод 11-й цифры по одной просто игнорируется, а не сдвигает номер
 */
internal fun phoneDigitsFromInput(input: String, previousDigits: String): String {
    val digits = input.filter { it.isDigit() }
    val isPastedWithCountryCode = digits.length == PHONE_DIGITS + 1 &&
        digits.length - previousDigits.length > 1 &&
        (digits.first() == '7' || digits.first() == '8')
    return (if (isPastedWithCountryCode) digits.drop(1) else digits).take(PHONE_DIGITS)
}

/**
 * Рисует «7011234567» как «(701) 123-45-67», не меняя сам текст в состоянии.
 * Поэтому в state и в запрос уходят только цифры, а курсор не прыгает при вводе скобок и дефисов
 */
internal object PhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = StringBuilder()
        // Позиция каждой исходной цифры в отформатированной строке — для перевода позиции курсора
        val positions = IntArray(digits.length)
        digits.forEachIndexed { index, digit ->
            when (index) {
                0 -> formatted.append('(')
                3 -> formatted.append(") ")
                6, 8 -> formatted.append('-')
            }
            positions[index] = formatted.length
            formatted.append(digit)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset < digits.length) positions[offset] else formatted.length

            override fun transformedToOriginal(offset: Int): Int =
                positions.count { it < offset }
        }
        return TransformedText(AnnotatedString(formatted.toString()), mapping)
    }
}
