package com.ort.firewolf.domain.model.village.participant

import java.net.URI

data class VillageParticipantNotificationCondition(
    val discordWebhookUrl: String,
    val village: VillageCondition,
    val message: MessageCondition,
) {
    data class VillageCondition(
        /** 村が開始した */
        val start: Boolean,
        /** 日付更新 */
        val dayChange: Boolean,
        /** 村がエピローグを迎えた */
        val epilogue: Boolean,
    )

    data class MessageCondition(
        /** 新着秘話 */
        val secretSay: Boolean,
        /** 新着役職窓発言 */
        val abilitySay: Boolean,
        /** アンカー */
        val anchor: Boolean,
        /** キーワード */
        val keywords: List<String>,
    )

    companion object {
        // 通知先として許可する Discord のホスト
        private val discordWebhookHosts = listOf("discord.com", "discordapp.com", "ptb.discord.com", "canary.discord.com")

        /**
         * 通知先が Discord の Webhook URL か
         * https で、Discord のホスト (ポート指定なし) の /api/webhooks/ 配下 (.. を含まない) だけを許可する
         */
        fun isValidDiscordWebhookUrl(url: String): Boolean {
            val uri = runCatching { URI(url) }.getOrNull() ?: return false
            if (uri.scheme != "https" || uri.rawUserInfo != null || uri.port != -1) return false
            val host = uri.host ?: return false
            if (discordWebhookHosts.none { it.equals(host, ignoreCase = true) }) return false
            val path = uri.rawPath ?: return false
            return path.startsWith("/api/webhooks/") && !path.contains("..")
        }
    }
}
