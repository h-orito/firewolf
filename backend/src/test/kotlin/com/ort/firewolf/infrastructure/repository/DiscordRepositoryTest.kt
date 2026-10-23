package com.ort.firewolf.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress

class DiscordRepositoryTest {
    private lateinit var server: HttpServer
    private val received = mutableListOf<String>()
    private var redirected = false

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/webhook") { exchange ->
            received.add(exchange.requestBody.readAllBytes().toString(Charsets.UTF_8))
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
        }
        server.createContext("/redirect") { exchange ->
            exchange.responseHeaders.add("Location", "${baseUrl()}/redirected")
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        server.createContext("/redirected") { exchange ->
            redirected = true
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
        }
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    private fun baseUrl() = "http://127.0.0.1:${server.address.port}"

    private fun repositoryAllowingAnyUrl() = DiscordRepository().apply { isAllowedUrl = { true } }

    @Test
    @DisplayName("メンションを解釈させない")
    fun postToWebhookDisablesMentions() {
        repositoryAllowingAnyUrl().postToWebhook("${baseUrl()}/webhook", 1, "text")

        assertEquals(1, received.size)
        val body = ObjectMapper().readTree(received[0])
        assertEquals("https://firewolf.netlify.app/village?id=1\ntext", body["content"].asText())
        assertEquals("FIREWOLF 1村通知", body["username"].asText())
        assertTrue(body["allowed_mentions"]["parse"].isArray)
        assertEquals(0, body["allowed_mentions"]["parse"].size())
    }

    @Test
    @DisplayName("Discord 以外の宛先には送らない")
    fun postToWebhookSkipsNotAllowedUrl() {
        DiscordRepository().postToWebhook("${baseUrl()}/webhook", 1, "text")

        assertTrue(received.isEmpty())
    }

    @Test
    @DisplayName("リダイレクトに従わない")
    fun postToWebhookDoesNotFollowRedirect() {
        repositoryAllowingAnyUrl().postToWebhook("${baseUrl()}/redirect", 1, "text")

        assertFalse(redirected)
    }

    @Test
    @DisplayName("送信先が落ちていても例外にしない")
    fun postToWebhookIgnoresUnreachableUrl() {
        val url = "${baseUrl()}/webhook"
        server.stop(0)
        repositoryAllowingAnyUrl().postToWebhook(url, 1, "text")
    }
}
