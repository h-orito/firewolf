package com.ort.firewolf.domain.model.village.setting

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VillagePasswordTest {
    @Test
    fun matches() {
        val password = VillagePassword("secret")
        assertTrue(password.matches("secret"))
        assertFalse(password.matches("wrong"))
        assertFalse(password.matches("secret "))
        assertFalse(password.matches(""))
        assertFalse(password.matches(null))
        // パスワードのない村は常に true
        assertTrue(VillagePassword(null).matches(null))
        assertTrue(VillagePassword("").matches("anything"))
    }
}
