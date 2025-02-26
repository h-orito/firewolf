package com.ort.firewolf.application.service

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.message.MessageQuery
import com.ort.firewolf.domain.model.message.Messages
import com.ort.firewolf.domain.model.skill.Skills
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.service.ability.AbilityDomainService
import com.ort.firewolf.domain.service.coming_out.ComingOutDomainService
import com.ort.firewolf.domain.service.commit.CommitDomainService
import com.ort.firewolf.domain.service.participate.ParticipateDomainService
import com.ort.firewolf.infrastructure.datasource.message.MessageDataSource
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messageDataSource: MessageDataSource,
    private val abilityDomainService: AbilityDomainService,
    private val participateDomainService: ParticipateDomainService,
    private val commitDomainService: CommitDomainService,
    private val comingOutDomainService: ComingOutDomainService,
    private val notificationService: NotificationService,
) {
    /**
     * 発言取得
     *
     * @param villageId villageId
     * @param villageDayId 村日付ID
     * @param query query
     * @return 発言
     */
    fun findMessages(
        villageId: Int,
        villageDayId: Int,
        query: MessageQuery
    ): Messages {
        return messageDataSource.findMessages(villageId, villageDayId, query)
    }

    /**
     * 最新発言日時取得
     *
     * @param villageId villageId
     * @param messageTypeList 発言種別
     * @param participant 参加情報
     * @param from この時間以降を取得
     * @return 発言
     */
    fun findLatestMessagesUnixTimeMilli(
        villageId: Int,
        messageTypeList: List<CDef.MessageType>,
        participant: VillageParticipant? = null,
        from: Long?
    ): Long {
        return messageDataSource.findLatestMessagesUnixTimeMilli(villageId, messageTypeList, participant, from)
    }

    /**
     * アンカー発言取得
     *
     * @param villageId villageId
     * @param messageType 発言種別
     * @param messageNumber 発言番号
     * @return 発言
     */
    fun findMessage(villageId: Int, messageType: CDef.MessageType, messageNumber: Int): Message? {
        return messageDataSource.findMessage(villageId, messageType, messageNumber)
    }

    /**
     * 参加者のその日の発言数を取得
     */
    fun findParticipateDayMessageList(
        villageId: Int,
        villageDay: com.ort.firewolf.domain.model.village.VillageDay,
        participant: VillageParticipant?
    ): Map<CDef.MessageType, Int> {
        participant ?: return mapOf()
        return messageDataSource.selectParticipateDayMessageList(villageId, villageDay.id, participant)
    }

    /**
     * 発言登録
     */
    fun registerMessage(village: Village, message: Message): Message {
        val registered = messageDataSource.registerMessage(village, message)
        notificationService.notifyToDeveloperIfNeeded(village.id, registered)
        return registered
    }

    /**
     * 村作成時のシステムメッセージ登録
     * @param village village
     */
    fun registerInitialMessage(village: Village) = registerMessage(village, village.createVillagePrologueMessage())

    /**
     * 村に参加する際の発言を登録
     */
    fun registerParticipateMessage(
        village: Village,
        participant: VillageParticipant,
        chara: Chara,
        charaName: String,
        charaShortName: String,
        message: String,
        isSpectate: Boolean
    ) {
        // {N}人目、{キャラ名}。
        messageDataSource.registerMessage(
            village,
            participateDomainService.createParticipateMessage(village, chara, charaName, charaShortName, isSpectate)
        )
        // 参加発言
        val messageContent = MessageContent.invoke(
            if (isSpectate) CDef.MessageType.見学発言.code() else CDef.MessageType.通常発言.code(),
            message,
            CDef.FaceType.通常.code()
        )
        registerMessage(
            village,
            Message.createSayMessage(participant, village.day.prologueDay().id, messageContent)
        )
    }

    /**
     * 名前変更する際のシステムメッセージを登録
     */
    fun registerChangeNameMessage(
        village: Village,
        before: VillageParticipant,
        after: VillageParticipant
    ) =
        registerMessage(
            village,
            participateDomainService.createChangeNameMessage(village, before, after)
        )

    /**
     * 退村する際のシステムメッセージを登録
     */
    fun registerLeaveMessage(village: Village, participant: VillageParticipant) =
        registerMessage(
            village,
            participateDomainService.createLeaveMessage(village, participant)
        )

    /**
     * 能力セットする際のシステムメッセージを登録
     * @param village village
     * @param participant 村参加者
     * @param targetId 対象の村参加者ID
     * @param abilityType abilityType
     * @param charas キャラ
     */
    fun registerAbilitySetMessage(
        village: Village,
        participant: VillageParticipant,
        targetId: Int?,
        abilityType: AbilityType,
        charas: Charas
    ) {
        val target = targetId?.let { village.participant.member(it) }
        val message: Message = abilityDomainService.createAbilitySetMessage(village, participant, target, abilityType)
        registerMessage(village, message)
    }

    /**
     * コミットする際のシステムメッセージを登録
     */
    fun registerCommitMessage(village: Village, myself: VillageParticipant, doCommit: Boolean) {
        registerMessage(
            village,
            commitDomainService.createCommitMessage(myself, doCommit, village.day.latestDay().id)
        )
    }

    fun registerComingOutMessage(village: Village, myself: VillageParticipant, skills: Skills) {
        registerMessage(
            village,
            comingOutDomainService.createComingOutMessage(myself, skills, village.day.latestDay().id)
        )
    }

    /**
     * 差分更新
     */
    fun updateDifference(village: Village, before: Messages, after: Messages) {
        messageDataSource.updateDifference(village, before, after)
    }
}