package com.ort.firewolf.domain.service.say

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class SecretSayDomainService {

    fun isViewable(village: Village, participant: VillageParticipant?): Boolean {
        // いずれかを満たせばok
        // 村として可能か
        if (village.isViewableSecretSay()) return true
        // 参加者として可能か
        participant ?: return false
        return participant.isViewableSecretSay()
    }

    fun isSayable(village: Village, participant: VillageParticipant): Boolean {
        // TODO 秘話
        return false
    }
}
