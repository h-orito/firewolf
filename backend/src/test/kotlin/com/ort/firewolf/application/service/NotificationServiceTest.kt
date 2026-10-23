package com.ort.firewolf.application.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NotificationServiceTest {
    @Test
    fun escapeDiscordMarkdown() {
        assertEquals("アリス", escapeDiscordMarkdown("アリス"))
        assertEquals("\\@everyone", escapeDiscordMarkdown("@everyone"))
        assertEquals("\\<\\@&123\\>", escapeDiscordMarkdown("<@&123>"))
        assertEquals("\\[click\\]\\(https\\://example.com\\)", escapeDiscordMarkdown("[click](https://example.com)"))
        assertEquals("\\*\\*bold\\*\\* \\_u\\_ \\~s\\~ \\|\\|x\\|\\| \\`c\\`", escapeDiscordMarkdown("**bold** _u_ ~s~ ||x|| `c`"))
        assertEquals("\\# 見出し", escapeDiscordMarkdown("# 見出し"))
        assertEquals("a\\\\\\*b", escapeDiscordMarkdown("a\\*b"))
    }
}
