package com.ort.firewolf.domain.service.rp

import com.ort.firewolf.domain.model.charachip.Charachips
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.myself.participant.ParticipantRpSituation
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class RpDomainService {

    fun convertToSituation(
        village: Village,
        myself: VillageParticipant?,
        charachips: Charachips,
        charas: Charas
    ): ParticipantRpSituation = ParticipantRpSituation(
        isAvailableChangeName = isAvailableChangeName(village, myself, charachips, charas),
    )

    private fun isAvailableChangeName(
        village: Village,
        myself: VillageParticipant?,
        charachips: Charachips,
        charas: Charas
    ): Boolean {
        if (!village.canChangeName() || myself == null || !myself.canChangeName(village.status.isEpilogue())) return false
        val chara = charas.chara(myself.charaId)
        return charachips.list.first { it.id == chara.charachipId }.isAvailableChangeName
    }
}