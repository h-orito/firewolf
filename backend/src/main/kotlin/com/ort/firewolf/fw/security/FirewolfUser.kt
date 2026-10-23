package com.ort.firewolf.fw.security

import com.ort.dbflute.allcommon.CDef
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class FirewolfUser(
    val uid: String,
    val authority: CDef.Authority,
    var ipAddress: String? = null,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(this.authority.code()))
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getUsername(): String {
        return this.uid
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String {
        return ""
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }
}

/**
 * 接続元の IP を返す。Cloudflare 配下のため、Cloudflare が必ず上書きする `CF-Connecting-IP` を使う。
 * `X-Forwarded-For` はクライアントが任意の値を送れるので使わない。
 * Cloudflare を通らない 80 番の経路は ingress で取得系 API だけに絞ってある。
 */
fun HttpServletRequest.getIpAddress(): String {
    val cfConnectingIp = this.getHeader("CF-Connecting-IP")
    return if (cfConnectingIp.isNullOrBlank()) {
        this.remoteAddr
    } else {
        cfConnectingIp.trim()
    }
}
