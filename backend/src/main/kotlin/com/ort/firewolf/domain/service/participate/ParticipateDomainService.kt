package com.ort.firewolf.domain.service.participate

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.myself.participant.VillageParticipateSituation
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class ParticipateDomainService {

    fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        player: Player?,
        charas: Charas
    ): VillageParticipateSituation {
        return VillageParticipateSituation(
            isParticipating = participant != null,
            isAvailableParticipate = isAvailableParticipate(player, village),
            isAvailableSpectate = isAvailableSpectate(player, village, charas.list.size),
            selectableCharaList = getSelectableCharaList(village, charas),
            isAvailableLeave = isAvailableLeave(village, participant),
            myself = participant
        )
    }

    /**
     * 参加チェック
     * @param player player
     * @param village village
     * @param charaId charaId
     * @param firstRequestSkill 第1役職希望
     * @param secondRequestSkill 第2役職希望
     * @param password 入村パスワード
     */
    fun assertParticipate(
        player: Player?,
        village: Village,
        charaId: Int,
        firstRequestSkill: CDef.Skill,
        secondRequestSkill: CDef.Skill,
        password: String?
    ) {
        // 参加できない状況ならNG
        if (!isAvailableParticipate(player, village)) throw FirewolfBusinessException("参加できません")
        // 既にそのキャラが参加していたりパスワードを間違えていたらNG
        village.assertParticipate(charaId, password)
        // 役職希望無効の場合はおまかせのみ
        if (!village.setting.rules.isValidSkillRequest(
                firstRequestSkill,
                secondRequestSkill
            )
        ) throw FirewolfBusinessException("希望役職が不正です")
    }

    /**
     * 見学チェック
     * @param player player
     * @param village village
     * @param charachipCharaNum 使用するキャラチップのキャラ数
     */
    fun assertSpectate(
        player: Player?,
        village: Village,
        charaId: Int,
        charachipCharaNum: Int,
        password: String?
    ) {
        if (!isAvailableSpectate(player, village, charachipCharaNum)) throw FirewolfBusinessException("見学できません")
        // 既にそのキャラが参加していたりパスワードを間違えていたらNG
        village.assertParticipate(charaId, password)
    }

    /**
     * 参加メッセージ
     */
    fun createParticipateMessage(
        village: Village,
        chara: Chara,
        charaName: String,
        charaShortName: String,
        isSpectate: Boolean
    ): Message {
        // 何人目か
        val number = if (isSpectate) {
            village.spectator.count
        } else {
            village.participant.count
        }
        val text = if (isSpectate) {
            "（見学）${number}人目、[${charaShortName}] ${charaName}。"
        } else {
            "${number}人目、[${charaShortName}] ${charaName}。"
        }
        return Message.createPublicSystemMessage(text, village.day.prologueDay().id)
    }

    /**
     * @param village village
     * @param charas charas
     * @return 参加/見学できるキャラ
     */
    fun getSelectableCharaList(village: Village, charas: Charas): List<Chara> {
        return charas.list.filterNot { chara ->
            village.participant.memberList.any { it.charaId == chara.id }
                    || village.spectator.memberList.any { it.charaId == chara.id }
        }
    }

    /**
     * @param village village
     * @param participant 参加者
     * @return 退村可能か
     */
    fun isAvailableLeave(
        village: Village,
        participant: VillageParticipant?
    ): Boolean {
        // 村として退村可能か
        if (!village.isAvailableLeave()) return false
        // 参加していない
        participant ?: return false

        return true
    }

    /**
     * 退村チェック
     * @param village village
     * @param participant 参加者
     */
    fun assertLeave(
        village: Village,
        participant: VillageParticipant?
    ) {
        if (!isAvailableLeave(village, participant)) throw FirewolfBusinessException("退村できません")
    }


    /**
     * 退村メッセージ
     */
    fun createLeaveMessage(village: Village, participant: VillageParticipant): Message =
        Message.createPublicSystemMessage(createLeaveMessageString(participant), village.day.latestDay().id)

    /**
     * 名前変更メッセージ
     */
    fun createChangeNameMessage(
        village: Village,
        before: VillageParticipant,
        after: VillageParticipant
    ): Message =
        Message.createPublicSystemMessage(
            createChangeNameMessageString(before, after),
            village.day.latestDay().id
        )

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * @param player player
     * @return 参加可能な状況か
     */
    private fun isAvailableParticipate(
        player: Player?,
        village: Village
    ): Boolean {
        // プレイヤーとして参加可能か
        player ?: return false
        if (!player.isAvailableParticipate(village.id)) return false
        // 村として参加可能か
        return village.isAvailableParticipate()
    }

    /**
     * @param player player
     * @param charachipCharaNum 使用するキャラチップのキャラ数
     * @return 見学可能な状況か
     */
    private fun isAvailableSpectate(
        player: Player?,
        village: Village,
        charachipCharaNum: Int
    ): Boolean {
        // プレイヤーとして参加可能か
        player ?: return false
        if (!player.isAvailableParticipate(village.id)) return false
        // 村として見学可能か
        return village.isAvailableSpectate(charachipCharaNum)
    }

    private fun createLeaveMessageString(participant: VillageParticipant): String =
        "${participant.name()}は村を去った。"

    private fun createChangeNameMessageString(before: VillageParticipant, after: VillageParticipant): String =
        "名前を変更しました。\n${before.name()} → ${after.name()}"
}