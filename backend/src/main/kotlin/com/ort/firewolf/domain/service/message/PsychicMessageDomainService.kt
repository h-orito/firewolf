package com.ort.firewolf.domain.service.message

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class PsychicMessageDomainService {

    fun isViewable(village: Village, participant: VillageParticipant?): Boolean {
        // いずれかを満たせばok
        // 村として可能か
        if (village.isViewablePsychicMessage()) return true
        // 参加者として可能か
        participant ?: return false
        return participant.isViewablePsychicMessage()
    }
}
