package com.ort.firewolf.domain.service.skill

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.myself.participant.VillageSkillRequestSituation
import com.ort.firewolf.domain.model.skill.SkillRequest
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBadRequestException
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class SkillRequestDomainService {

    fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        skillRequest: SkillRequest?
    ): VillageSkillRequestSituation {
        return VillageSkillRequestSituation(
            availableSkillRequest = isAvailableSkillRequest(village, participant),
            selectableSkillList = village.setting.organizations.allRequestableSkillList(),
            skillRequest = skillRequest
        )
    }

    /**
     * @param village village
     * @param participant 参加者
     * @return 役職希望可能か
     */
    fun isAvailableSkillRequest(
        village: Village,
        participant: VillageParticipant?
    ): Boolean {
        // 村として可能か
        if (!village.isAvailableSkillRequest()) return false
        // 参加者として可能か
        participant ?: return true
        return participant.isAvailableSkillRequest()
    }

    /**
     * 役職希望変更チェック
     * @param village village
     * @param participant 参加者
     * @param firstRequestSkill 第1役職希望
     * @param secondRequestSkill 第2役職希望
     */
    fun assertSkillRequest(
        village: Village,
        participant: VillageParticipant?,
        firstRequestSkill: String,
        secondRequestSkill: String
    ) {
        if (!isAvailableSkillRequest(village, participant)) throw FirewolfBusinessException("役職希望変更できません")
        val first = CDef.Skill.codeOf(firstRequestSkill) ?: throw FirewolfBadRequestException("第1希望が不正")
        val second = CDef.Skill.codeOf(secondRequestSkill) ?: throw FirewolfBadRequestException("第1希望が不正")
        village.assertSkillRequest(first, second)
    }
}