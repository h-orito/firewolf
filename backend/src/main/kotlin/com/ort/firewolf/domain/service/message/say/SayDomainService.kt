package com.ort.firewolf.domain.service.message.say

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.CharaFace
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.message.MessageType
import com.ort.firewolf.domain.model.myself.participant.VillageSayMessageTypeSituation
import com.ort.firewolf.domain.model.myself.participant.VillageSayRestrictSituation
import com.ort.firewolf.domain.model.myself.participant.VillageSaySituation
import com.ort.firewolf.domain.model.player.Player
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
        player: Player?,
        participant: VillageParticipant?,
        charas: Charas,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>,
    ): VillageSaySituation {
        val selectableMessageTypeList = getSelectableMessageTypeList(
            village,
            participant,
            player,
            latestDayMessageCountMap
        )
        return VillageSaySituation(
            isAvailableSay = isAvailableSay(village, participant),
            selectableMessageTypeList = selectableMessageTypeList,
            selectableFaceTypeList = getSelectableFaceTypeList(participant, charas),
            defaultMessageType = detectDefaultMessageType(
                isAvailableSay(village, participant),
                selectableMessageTypeList
            )
        )
    }

    fun assertSay(
        village: Village,
        myself: VillageParticipant,
        player: Player,
        chara: Chara,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>,
        messageContent: MessageContent,
    ) {
        // 事前チェック
        if (!isAvailableSay(village, myself)) throw FirewolfBusinessException("発言できません")
        // 発言種別ごとのチェック
        if (!detectSayTypeService(messageContent.type.toCdef()).isSayable(village, myself, player)) {
            throw FirewolfBadRequestException("発言できません")
        }
        // 表情種別チェック
        if (!isSelectableFaceType(chara, messageContent)) throw FirewolfBadRequestException("不正な表情種別です")
        // 発言回数、長さ、行数チェック
        assertMessageRestrict(village, myself, messageContent, latestDayMessageCountMap)
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
        village.assertMessageRestrict(messageContent, mapOf())
    }

    fun detectSayTypeService(type: CDef.MessageType): SayTypeDomainService {
        return when (type) {
            CDef.MessageType.通常発言 -> normalSayDomainService
            CDef.MessageType.人狼の囁き -> werewolfSayDomainService
            CDef.MessageType.共鳴発言 -> sympathizeSayDomainService
            CDef.MessageType.死者の呻き -> graveSayDomainService
            CDef.MessageType.独り言 -> monologueSayDomainService
            CDef.MessageType.見学発言 -> spectateSayDomainService
            CDef.MessageType.アクション -> actionSayDomainService
            CDef.MessageType.秘話 -> secretSayDomainService
            else -> throw FirewolfBadRequestException("不正な発言種別です")
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isSelectableFaceType(chara: Chara, messageContent: MessageContent): Boolean =
        messageContent.type.toCdef() == CDef.MessageType.アクション
                || chara.faceList.any { it.type == messageContent.faceCode }

    private fun isAvailableSay(village: Village, myself: VillageParticipant?): Boolean {
        myself ?: return false
        return village.isAvailableSay() && myself.isAvailableSay(village.status.isEpilogue())
    }

    private fun getSelectableMessageTypeList(
        village: Village,
        myself: VillageParticipant?,
        player: Player?,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>,
    ): List<VillageSayMessageTypeSituation> {
        if (!isAvailableSay(village, myself)) return listOf()
        return MessageType.sayTypeList
            .filter { detectSayTypeService(it).isSayable(village, myself!!, player) }
            .map {
                convertToMessageTypeSituation(
                    village,
                    myself!!,
                    latestDayMessageCountMap,
                    it
                )
            }
    }

    private fun convertToMessageTypeSituation(
        village: Village,
        myself: VillageParticipant,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>,
        messageType: CDef.MessageType
    ): VillageSayMessageTypeSituation {
        val targetList =
            if (messageType != CDef.MessageType.秘話) emptyList()
            else (village.participant.memberList + village.spectator.memberList).filterNot { it.id == myself.id }
        return VillageSayMessageTypeSituation(
            messageType = MessageType(messageType),
            restrict = convertToRestrictSituation(village, latestDayMessageCountMap, messageType),
            targetList = targetList
        )
    }

    private fun convertToRestrictSituation(
        village: Village,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>,
        messageType: CDef.MessageType
    ): VillageSayRestrictSituation {
        val restrict = village.setting.rules.messageRestrict.restrict(messageType)
        return VillageSayRestrictSituation(
            restricted = restrict != null,
            maxCount = restrict?.count,
            remainingCount = restrict?.remainingCount(
                village.status.toCdef(),
                latestDayMessageCountMap
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

    private fun assertMessageRestrict(
        village: Village,
        myself: VillageParticipant,
        messageContent: MessageContent,
        latestDayMessageCountMap: Map<CDef.MessageType, Int>
    ) {
        if (myself.isAdmin()) return
        village.assertMessageRestrict(messageContent, latestDayMessageCountMap)
    }
}