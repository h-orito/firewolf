package com.ort.firewolf.domain.service.say

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class SecretSayDomainService {

    fun isViewable(village: Village, participant: VillageParticipant?): Boolean {
        // 村として可能か
        return village.isViewableMonologueSay()
    }

    fun isSayable(village: Village, participant: VillageParticipant): Boolean {
        return village.isSayableSecretSay() && participant.isSayableSecretSay()
    }

    fun assertSay(village: Village, myself: VillageParticipant, targetId: Int?) {
        targetId ?: throw FirewolfBusinessException("発言できません")
        if (targetId == myself.id || village.allParticipants().memberList.none { it.id == targetId }) {
            throw FirewolfBusinessException("発言できません")
        }
        if (!isSayable(village, myself)) throw FirewolfBusinessException("発言できません")
    }
}
