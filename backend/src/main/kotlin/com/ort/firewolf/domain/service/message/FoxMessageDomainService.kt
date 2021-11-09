package com.ort.firewolf.domain.service.message

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

/**
 * 妖狐メッセージ
 */
@Service
class FoxMessageDomainService {

    fun isViewable(village: Village, participant: VillageParticipant?): Boolean {
        // いずれかを満たせばok
        // 村として可能か
        if (village.isViewableFoxMessage()) return true
        // 参加者として可能か
        participant ?: return false
        return participant.isViewableFoxMessage()
    }
}
