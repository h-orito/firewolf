package com.ort.firewolf.domain.model.village.participant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class VillageParticipantNotificationConditionTest {
    @ParameterizedTest
    @CsvSource(
        "https://discord.com/api/webhooks/123/abc, true",
        "https://discordapp.com/api/webhooks/123/abc, true",
        "https://ptb.discord.com/api/webhooks/123/abc, true",
        "https://canary.discord.com/api/webhooks/123/abc, true",
        "https://DISCORD.com/api/webhooks/123/abc, true",
        "http://discord.com/api/webhooks/123/abc, false",
        "https://discord.com:8443/api/webhooks/123/abc, false",
        "https://user:pass@discord.com/api/webhooks/123/abc, false",
        "https://discord.com/api/users/123, false",
        "https://discord.com/api/webhooks, false",
        "https://discord.com/api/webhooks/../../users/@me, false",
        "https://discord.com.example.com/api/webhooks/123/abc, false",
        "https://evil.discord.com/api/webhooks/123/abc, false",
        "http://127.0.0.1/api/webhooks/123/abc, false",
        "https://169.254.169.254/api/webhooks/123/abc, false",
        "discord.com/api/webhooks/123/abc, false",
        "'', false",
        "'https://discord.com/api/webhooks/1 2', false",
    )
    fun isValidDiscordWebhookUrl(
        url: String,
        expected: Boolean,
    ) {
        assertEquals(expected, VillageParticipantNotificationCondition.isValidDiscordWebhookUrl(url), url)
    }
}
