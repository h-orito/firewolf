package com.ort.firewolf.fw.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class FirewolfUserTest {
    private fun request(headers: Map<String, String>): MockHttpServletRequest =
        MockHttpServletRequest().apply {
            remoteAddr = "10.42.0.10"
            headers.forEach { (name, value) -> addHeader(name, value) }
        }

    @Test
    @DisplayName("CF-Connecting-IP があればそれを使う")
    fun usesCfConnectingIp() {
        val req = request(mapOf("CF-Connecting-IP" to "203.0.113.5"))
        assertEquals("203.0.113.5", req.getIpAddress())
    }

    @Test
    @DisplayName("X-Forwarded-For は使わない")
    fun ignoresXForwardedFor() {
        val req =
            request(
                mapOf(
                    "X-Forwarded-For" to "198.51.100.1, 203.0.113.5, 172.64.0.1",
                    "CF-Connecting-IP" to "203.0.113.5",
                ),
            )
        assertEquals("203.0.113.5", req.getIpAddress())
        assertEquals("10.42.0.10", request(mapOf("X-Forwarded-For" to "198.51.100.1")).getIpAddress())
    }

    @Test
    @DisplayName("CF-Connecting-IP が無いか空なら接続元のアドレスを使う")
    fun fallsBackToRemoteAddr() {
        assertEquals("10.42.0.10", request(emptyMap()).getIpAddress())
        assertEquals("10.42.0.10", request(mapOf("CF-Connecting-IP" to " ")).getIpAddress())
    }
}
