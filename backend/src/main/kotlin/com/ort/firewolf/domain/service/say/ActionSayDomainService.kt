package com.ort.firewolf.domain.service.say

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class ActionSayDomainService {

    fun isViewable(village: Village, myself: VillageParticipant?): Boolean = true

    fun isSayable(village: Village, myself: VillageParticipant): Boolean =
        myself.isAdmin() || village.isSayableActionSay()

    fun assertSay(village: Village, myself: VillageParticipant) {
        if (!isSayable(village, myself)) throw FirewolfBusinessException("発言できません")
    }
}
