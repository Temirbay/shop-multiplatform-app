package kz.mtemirbay.shop.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthValidationTest {

    @Test
    fun validRegistrationHasNoErrors() {
        assertEquals(emptyList(), AuthValidation.validate(RegisterRequest("Мирас", "+77011234567", "password1")))
    }

    @Test
    fun passwordLengthLimits() {
        assertEquals(AuthValidationError.PASSWORD_TOO_SHORT, AuthValidation.validatePassword("1234567"))
        assertNull(AuthValidation.validatePassword("12345678"))
        assertEquals(AuthValidationError.PASSWORD_TOO_LONG, AuthValidation.validatePassword("a".repeat(65)))
    }

    @Test
    fun nameAndPhoneReuseOrderRules() {
        assertEquals(AuthValidationError.NAME_EMPTY, AuthValidation.validateName(" "))
        assertEquals(AuthValidationError.PHONE_INVALID, AuthValidation.validatePhone("87011234567"))
        assertNull(AuthValidation.validatePhone("+7 (701) 123-45-67"))
    }
}
