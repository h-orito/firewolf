package com.ort.firewolf.domain.service.say

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.CharaFace
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.message.MessageType
import com.ort.firewolf.domain.model.myself.participant.VillageSayMessageTypeSituation
import com.ort.firewolf.domain.model.myself.participant.VillageSayRestrictSituation
import com.ort.firewolf.domain.model.myself.participant.VillageSaySituation
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBadRequestException
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class SayDomainService(
    private val normalSayDomainService: NormalSayDomainService,
    private val graveSayDomainService: GraveSayDomainService,
    private val monologueSayDomainService: MonologueSayDomainService,
    private val spectateSayDomainService: SpectateSayDomainService,
    private val werewolfSayDomainService: WerewolfSayDomainService,
    private val sympathizeSayDomainService: SympathizeSayDomainService,
    private val actionSayDomainService: ActionSayDomainService,
    private val secretSayDomainService: SecretSayDomainService
) {

    private val defaultMessageTypeOrder = listOf(
        CDef.MessageType.人狼の囁き,
        CDef.MessageType.共鳴発言,
        CDef.MessageType.通常発言,
        CDef.MessageType.死者の呻き,
        CDef.MessageType.見学発言,
        CDef.MessageType.独り言
    )

    fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        charas: Charas,
        latestDayMessageList: List<Message>
    ): VillageSaySituation {
        return VillageSaySituation(
            isAvailableSay = isAvailableSay(village, participant),
            selectableMessageTypeList = getSelectableMessageTypeList(village, participant, latestDayMessageList),
            selectableFaceTypeList = getSelectableFaceTypeList(participant, charas),
            defaultMessageType = detectDefaultMessageType(
                isAvailableSay(village, participant),
                getSelectableMessageTypeList(village, participant, latestDayMessageList)
            )
        )
    }

    fun assertSay(
        village: Village,
        myself: VillageParticipant?,
        chara: Chara?,
        latestDayMessageList: List<Message>,
        messageContent: MessageContent,
        targetId: Int? = null
    ) {
        // 事前チェック
        if (!isAvailableSay(village, myself)) throw FirewolfBusinessException("発言できません")
        // 発言種別ごとのチェック
        when (messageContent.type.toCdef()) {
            CDef.MessageType.通常発言 -> normalSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.人狼の囁き -> werewolfSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.共鳴発言 -> sympathizeSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.死者の呻き -> graveSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.独り言 -> monologueSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.見学発言 -> spectateSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.アクション -> actionSayDomainService.assertSay(village, myself!!)
            CDef.MessageType.秘話 -> secretSayDomainService.assertSay(village, myself!!, targetId)
            else -> throw FirewolfBadRequestException("不正な発言種別です")
        }
        // 表情種別チェック
        if (!isSelectableFaceType(chara!!, messageContent)) throw FirewolfBadRequestException("不正な表情種別です")
        // 発言回数、長さ、行数チェック
        village.assertMessageRestrict(messageContent, latestDayMessageList)
    }

    fun assertCreatorSay(
        village: Village,
        messageContent: MessageContent
    ) {
        // 事前チェック
        if (!village.isAvailableSay()) throw FirewolfBusinessException("発言できません")
        // 長さ、行数チェック
        messageContent.assertMessageLength(400, 40)
    }

    fun assertParticipateSay(
        village: Village,
        chara: Chara?,
        messageContent: MessageContent
    ) {
        // 事前チェック
        if (!village.isAvailableSay()) throw FirewolfBusinessException("入村発言できません")
        // 表情種別チェック
        if (!isSelectableFaceType(chara!!, messageContent)) throw FirewolfBadRequestException("不正な表情種別です")
        // 発言長さ、行数チェック
        village.assertMessageRestrict(messageContent, listOf())
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isSelectableFaceType(chara: Chara, messageContent: MessageContent): Boolean =
        messageContent.type.toCdef() == CDef.MessageType.アクション
                || chara.faceList.any { it.type == messageContent.faceCode }

    private fun isAvailableSay(village: Village, participant: VillageParticipant?): Boolean {
        // 参加者として可能か
        participant ?: return false
        if (!participant.isAvailableSay(village.status.toCdef() == CDef.VillageStatus.エピローグ)) return false
        // 村として可能か
        if (!village.isAvailableSay()) return false
        return true
    }

    private fun getSelectableMessageTypeList(
        village: Village,
        myself: VillageParticipant?,
        latestDayMessageList: List<Message>
    ): List<VillageSayMessageTypeSituation> {
        if (!isAvailableSay(village, myself)) return listOf()

        val list: MutableList<VillageSayMessageTypeSituation> = mutableListOf()

        if (normalSayDomainService.isSayable(village, myself!!)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.通常発言
            )
        )
        if (werewolfSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.人狼の囁き
            )
        )
        if (sympathizeSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.共鳴発言
            )
        )
        if (graveSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.死者の呻き
            )
        )
        if (monologueSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.独り言
            )
        )
        if (spectateSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.見学発言
            )
        )
        if (actionSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.アクション
            )
        )
        if (secretSayDomainService.isSayable(village, myself)) list.add(
            convertToMessageTypeSituation(
                village,
                myself,
                latestDayMessageList,
                CDef.MessageType.秘話
            )
        )

        return list
    }

    private fun convertToMessageTypeSituation(
        village: Village,
        myself: VillageParticipant,
        latestDayMessageList: List<Message>,
        messageType: CDef.MessageType
    ): VillageSayMessageTypeSituation {
        val targetList =
            if (messageType != CDef.MessageType.秘話) emptyList()
            else (village.participant.memberList + village.spectator.memberList).filterNot { it.id == myself.id }
        return VillageSayMessageTypeSituation(
            messageType = MessageType(messageType),
            restrict = convertToRestrictSituation(village, latestDayMessageList, messageType),
            targetList = targetList
        )
    }

    private fun convertToRestrictSituation(
        village: Village,
        latestDayMessageList: List<Message>,
        messageType: CDef.MessageType
    ): VillageSayRestrictSituation {
        val restrict = village.setting.rules.messageRestrict.restrict(messageType)
        return VillageSayRestrictSituation(
            restricted = restrict != null,
            maxCount = restrict?.count,
            remainingCount = restrict?.remainingCount(
                village.status.toCdef(),
                latestDayMessageList
            ),
            maxLength = restrict?.length ?: MessageContent.defaultLengthMax,
            maxLine = restrict?.line ?: MessageContent.defaultLineMax
        )
    }

    private fun getSelectableFaceTypeList(participant: VillageParticipant?, charas: Charas): List<CharaFace> {
        if (participant == null) return listOf()
        return charas.chara(participant.charaId).faceList
    }

    private fun detectDefaultMessageType(
        availableSay: Boolean,
        selectableMessageTypeList: List<VillageSayMessageTypeSituation>
    ): MessageType? {
        if (!availableSay || selectableMessageTypeList.isEmpty()) return null
        val selectableMessageTypeCdefList = selectableMessageTypeList.map { it.messageType.toCdef() }

        defaultMessageTypeOrder.forEach { cdefMessageType ->
            if (selectableMessageTypeCdefList.contains(cdefMessageType)) return MessageType(cdefMessageType)
        }
        return null
    }
}