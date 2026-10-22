package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneMaskTest {

    @Test
    fun formatsDigitsAsTheyAreTyped() {
        assertEquals("", format(""))
        assertEquals("(7", format("7"))
        assertEquals("(701", format("701"))
        assertEquals("(701) 1", format("7011"))
        assertEquals("(701) 123-4", format("7011234"))
        assertEquals("(701) 123-45-67", format("7011234567"))
    }

    @Test
    fun cursorStaysAfterTheSameDigit() {
        val mapping = PhoneVisualTransformation.filter(AnnotatedString("7011234567")).offsetMapping

        assertEquals(1, mapping.originalToTransformed(0))     // перед первой цифрой — после «(»
        assertEquals(7, mapping.originalToTransformed(4))     // перед 5-й цифрой — сразу после «(701) 1»
        assertEquals(15, mapping.originalToTransformed(10))   // в конце
        assertEquals(4, mapping.transformedToOriginal(7))
        assertEquals(10, mapping.transformedToOriginal(15))
    }

    @Test
    fun keepsOnlyTenDigits() {
        assertEquals("7011234567", phoneDigitsFromInput("(701) 123-45-67", previousDigits = "701123456"))
        assertEquals("7011234567", phoneDigitsFromInput("70112345678", previousDigits = "7011234567"))
    }

    @Test
    fun pastedNumberWithCountryCodeDropsIt() {
        assertEquals("7011234567", phoneDigitsFromInput("+7 701 123 45 67", previousDigits = ""))
        assertEquals("7011234567", phoneDigitsFromInput("8 (701) 123-45-67", previousDigits = ""))
    }

    private fun format(digits: String) = PhoneVisualTransformation.filter(AnnotatedString(digits)).text.text
}
