package com.ort.firewolf.domain.service.say

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class SympathizeSayDomainService {

    fun isViewable(village: Village, participant: VillageParticipant?): Boolean {
        // いずれかを満たせばok
        // 村として可能か
        if (village.isViewableSympathizeSay()) return true
        // 参加者として可能か
        participant ?: return false
        return participant.isViewableSympathizeSay()
    }

    fun isSayable(village: Village, participant: VillageParticipant): Boolean {
        // 参加者として可能か
        if (!participant.isSayableSympathizeSay()) return false
        // 村として可能か
        return village.isSayableSympathizeSay()
    }

    fun assertSay(village: Village, participant: VillageParticipant) {
        if (!isSayable(village, participant)) throw FirewolfBusinessException("発言できません")
    }
}
