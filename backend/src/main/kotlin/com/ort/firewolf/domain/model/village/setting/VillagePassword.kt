package com.ort.firewolf.domain.model.village.setting

import java.security.MessageDigest

data class VillagePassword(
    val joinPasswordRequired: Boolean,
    val joinPassword: String?,
) {
    companion object {
        operator fun invoke(joinPassword: String?): VillagePassword {
            return VillagePassword(
                joinPasswordRequired = !joinPassword.isNullOrEmpty(),
                joinPassword = joinPassword,
            )
        }
    }

    /** 入村パスワードが合っているか (パスワードのない村は常に true)。一定時間で比較する */
    fun matches(password: String?): Boolean {
        if (!joinPasswordRequired) return true
        val expected = joinPassword ?: return false
        password ?: return false
        return MessageDigest.isEqual(expected.toByteArray(), password.toByteArray())
    }

    fun existsDifference(password: VillagePassword): Boolean {
        return joinPassword != password.joinPassword
    }
}
