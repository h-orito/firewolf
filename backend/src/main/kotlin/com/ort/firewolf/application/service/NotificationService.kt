package com.ort.firewolf.application.service

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.service.message.MessageDomainService
import com.ort.firewolf.infrastructure.repository.DiscordRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val discordRepository: DiscordRepository,
    private val messageDomainService: MessageDomainService
) {

    fun notifyToDeveloperIfNeeded(villageId: Int, message: Message) {
        if (!message.shouldNotify()) return
        discordRepository.post(villageId, message.content.text)
    }

    fun notifyToDeveloperTextIfNeeded(village: Village, text: String) {
        discordRepository.post(village.id, text)
    }

    fun notifyVillageStartToCustomerIfNeeded(village: Village) {
        village.allParticipants().memberList.filter {
            it.notification?.village?.start ?: false
        }.forEach {
            discordRepository.postToWebhook(
                webhookUrl = it.notification!!.discordWebhookUrl,
                villageId = village.id,
                message = "村が開始されました。"
            )
        }
    }

    fun notifyVillageEpilogueToCustomerIfNeeded(village: Village) {
        village.allParticipants().memberList.filter {
            it.notification?.village?.epilogue ?: false
        }.forEach {
            discordRepository.postToWebhook(
                webhookUrl = it.notification!!.discordWebhookUrl,
                villageId = village.id,
                message = "村がエピローグを迎えました。"
            )
        }
    }

    fun notifyVillageDaychangeToCustomerIfNeeded(village: Village) {
        village.allParticipants().memberList.filter {
            it.notification?.village?.dayChange ?: false
        }.forEach {
            discordRepository.postToWebhook(
                webhookUrl = it.notification!!.discordWebhookUrl,
                villageId = village.id,
                message = "村の日付が${village.day.latestDay().day}日目に更新されました。"
            )
        }
    }

    fun notifyTest(url: String, villageId: Int) {
        discordRepository.postToWebhook(
            webhookUrl = url,
            villageId = villageId,
            message = "テスト通知です。"
        )
    }

    fun notifyReceiveMessageToCustomerIfNeeded(
        village: Village,
        players: Players,
        message: Message
    ) {
        // 秘話→アンカー→キーワード→役職窓の順
        val alreadyNotifiedParticipantIds = mutableListOf<Int>()
        notifyReceiveSecretSayToCustomerIfNeeded(village, message)?.let {
            alreadyNotifiedParticipantIds.add(it)
        }
        val idsByAnchor =
            notifyReceiveAnchorToCustomerIfNeeded(village, players, message, alreadyNotifiedParticipantIds)
        alreadyNotifiedParticipantIds.addAll(idsByAnchor)
        val idsByKeyword =
            notifyReceiveKeywordToCustomerIfNeeded(village, players, message, alreadyNotifiedParticipantIds)
        alreadyNotifiedParticipantIds.addAll(idsByKeyword)
        notifyReceiveAbilitySayToCustomerIfNeeded(village, players, message, alreadyNotifiedParticipantIds)
    }

    private fun notifyReceiveSecretSayToCustomerIfNeeded(
        village: Village,
        message: Message,
    ): Int? {
        if (message.content.type.toCdef() != CDef.MessageType.秘話) return null
        val toParticipant =
            village.allParticipants().memberList.find { it.id == message.toVillageParticipantId } ?: return null
        val fromParticipant =
            village.allParticipants().memberList.find { it.id == message.fromVillageParticipantId } ?: return null
        return toParticipant.notification?.let {
            if (!it.message.secretSay) return null
            discordRepository.postToWebhook(
                webhookUrl = it.discordWebhookUrl,
                villageId = village.id,
                message = "${fromParticipant.name()}から秘話が届きました。",
                shouldContainVillageUrl = false
            )
            return toParticipant.id
        }
    }

    private fun notifyReceiveAnchorToCustomerIfNeeded(
        village: Village,
        players: Players,
        message: Message,
        alreadyNotifiedParticipantIds: List<Int>
    ): List<Int> {
        val notifiedParticipantIds = mutableListOf<Int>()
        val fromParticipant = village.allParticipants().memberList.find { it.id == message.fromVillageParticipantId }

        village.allParticipants().memberList
            .asSequence()
            .filterNot { it.id == message.fromVillageParticipantId } // 自分の発言でない
            .filterNot { alreadyNotifiedParticipantIds.contains(it.id) }
            .filter {
                val player = players.player(it.playerId!!)
                messageDomainService.isViewableMessage(
                    village,
                    it,
                    player,
                    message.content.type.toCdef(),
                    village.day.latestDay().day
                )
            }.filter {
                it.notification?.message?.anchor ?: false
            }.filter {
                message.sendToParticipantIds.contains(it.id)
            }
            .toList().forEach {
                val messageTypeName = message.content.type.name
                val text = if (fromParticipant == null) {
                    "${messageTypeName}であなたの発言がアンカー指定されました。"
                } else {
                    "${fromParticipant.name()}の${messageTypeName}であなたの発言がアンカー指定されました。"
                }
                discordRepository.postToWebhook(
                    webhookUrl = it.notification!!.discordWebhookUrl,
                    villageId = village.id,
                    message = text,
                    shouldContainVillageUrl = false
                )
                notifiedParticipantIds.add(it.id)
            }
        return notifiedParticipantIds
    }

    private fun notifyReceiveKeywordToCustomerIfNeeded(
        village: Village,
        players: Players,
        message: Message,
        alreadyNotifiedParticipantIds: List<Int>
    ): List<Int> {
        val notifiedParticipantIds = mutableListOf<Int>()
        val fromParticipant = village.allParticipants().memberList.find { it.id == message.fromVillageParticipantId }

        village.allParticipants().memberList
            .asSequence()
            .filter {
                val keywords = it.notification?.message?.keywords
                val player = players.player(it.playerId!!)

                it.id != message.fromVillageParticipantId &&  // 自分の発言でない
                        !alreadyNotifiedParticipantIds.contains(it.id) &&  // 通知済みでない
                        // 発言を閲覧できる
                        messageDomainService.isViewableMessage(
                            village = village,
                            myself = it,
                            player = player,
                            messageType = message.content.type.toCdef(),
                            day = village.day.latestDay().day
                        )
                        // キーワードが含まれている
                        && !keywords.isNullOrEmpty()
                        && keywords.any { keyword -> message.content.text.contains(keyword) }
            }
            .toList().forEach {
                val messageTypeName = message.content.type.name
                val text = if (fromParticipant == null) {
                    "${messageTypeName}に指定キーワードが含まれています。"
                } else {
                    "${fromParticipant.name()}の${messageTypeName}に指定キーワードが含まれています。"
                }
                discordRepository.postToWebhook(
                    webhookUrl = it.notification!!.discordWebhookUrl,
                    villageId = village.id,
                    message = text,
                    shouldContainVillageUrl = false
                )
                notifiedParticipantIds.add(it.id)
            }
        return notifiedParticipantIds
    }

    private fun notifyReceiveAbilitySayToCustomerIfNeeded(
        village: Village,
        players: Players,
        message: Message,
        alreadyNotifiedParticipantIds: List<Int>
    ) {
        if (!message.content.type.isOwlViewableType()) return
        val fromParticipant =
            village.participant.memberList.find { it.id == message.fromVillageParticipantId } ?: return
        village.allParticipants().memberList
            .filterNot { it.id == message.fromVillageParticipantId } // 自分の発言でない
            .filterNot { alreadyNotifiedParticipantIds.contains(it.id) } // 既にアンカーで通知していたら通知しない
            .filter {
                val player = players.player(it.playerId!!)
                messageDomainService.isViewableMessage(
                    village,
                    it,
                    player,
                    message.content.type.toCdef(),
                    village.day.latestDay().day
                )
            }.filter {
                it.notification?.message?.abilitySay ?: false
            }.forEach {
                discordRepository.postToWebhook(
                    webhookUrl = it.notification!!.discordWebhookUrl,
                    villageId = village.id,
                    message = "${fromParticipant.name()}から${message.content.type.name}が届きました。",
                    shouldContainVillageUrl = false
                )
            }
    }
}