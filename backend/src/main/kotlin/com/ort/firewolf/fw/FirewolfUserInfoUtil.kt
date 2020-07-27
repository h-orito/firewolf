package com.ort.firewolf.fw

import com.ort.firewolf.fw.security.FirewolfUser
import org.springframework.security.core.context.SecurityContextHolder


class FirewolfUserInfoUtil private constructor() {
    companion object {
        fun getUserInfo(): FirewolfUser? {
            val authentication = SecurityContextHolder.getContext().authentication ?: return null
            return if (authentication.principal is FirewolfUser) {
                FirewolfUser::class.java!!.cast(authentication.principal)
            } else null
        }
    }
}