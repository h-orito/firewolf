package com.ort.firewolf.infrastructure.repository

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.ort.firewolf.domain.model.village.participant.VillageParticipantNotificationCondition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Repository
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import java.time.Duration

@Repository
class DiscordRepository {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordRepository::class.java)

        private val CONNECT_TIMEOUT = Duration.ofSeconds(5)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
    }

    @Value("\${discord.webhook-url:}")
    private lateinit var webhookUrl: String

    @Value("\${discord.master-userid:}")
    private lateinit var masterUserId: String

    // 送信してよい宛先か (保存時の確認より前に登録された宛先にも送らないため、送信時にも確認する)
    internal var isAllowedUrl: (String) -> Boolean = { VillageParticipantNotificationCondition.isValidDiscordWebhookUrl(it) }

    private val restTemplate =
        RestTemplate(
            object : SimpleClientHttpRequestFactory() {
                override fun prepareConnection(
                    connection: HttpURLConnection,
                    httpMethod: String,
                ) {
                    super.prepareConnection(connection, httpMethod)
                    // 許可した宛先以外に送らないよう、リダイレクトには従わない
                    connection.instanceFollowRedirects = false
                }
            }.apply {
                setConnectTimeout(CONNECT_TIMEOUT)
                setReadTimeout(READ_TIMEOUT)
            },
        )

    fun post(
        villageId: Int,
        message: String,
    ) {
        if (webhookUrl.isEmpty()) return
        val request =
            Request(
                content = "<@!$masterUserId>\nhttps://firewolf.netlify.app/village?id=$villageId\n$message",
                // 開発者へのメンションだけを許可する
                allowedMentions = AllowedMentions(users = listOf(masterUserId)),
            )
        send(webhookUrl, request)
    }

    fun postToWebhook(
        webhookUrl: String,
        villageId: Int,
        message: String,
        shouldContainVillageUrl: Boolean = true,
    ) {
        if (!isAllowedUrl(webhookUrl)) {
            logger.warn("discord投稿をスキップしました: 許可されていない宛先です (village {})", villageId)
            return
        }
        val content =
            if (shouldContainVillageUrl) {
                "https://firewolf.netlify.app/village?id=$villageId\n$message"
            } else {
                message
            }
        val request =
            Request(
                content = content,
                username = "FIREWOLF ${villageId}村通知",
            )
        send(webhookUrl, request)
    }

    private fun send(
        url: String,
        request: Request,
    ) {
        try {
            val formHeaders = HttpHeaders()
            formHeaders.contentType = MediaType.APPLICATION_JSON
            formHeaders.add(HttpHeaders.USER_AGENT, "RestTemplate")
            val formEntity = HttpEntity(request, formHeaders)
            restTemplate.exchange(url, HttpMethod.POST, formEntity, String::class.java)
        } catch (e: HttpStatusCodeException) {
            logger.error("discord投稿に失敗しました: status {}", e.statusCode.value())
        } catch (e: Exception) {
            // 例外のメッセージには webhook URL (トークンを含む) が入るため、ログには種類だけを出す
            logger.error("discord投稿に失敗しました: {} ({})", e.javaClass.simpleName, e.cause?.javaClass?.simpleName)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Request(
        val content: String,
        val username: String? = null,
        // メンションは既定では一切解釈させない
        @get:JsonProperty("allowed_mentions")
        val allowedMentions: AllowedMentions = AllowedMentions(),
    ) : java.io.Serializable

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class AllowedMentions(
        val parse: List<String> = emptyList(),
        val users: List<String>? = null,
    ) : java.io.Serializable
}
