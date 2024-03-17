package com.ort.firewolf.domain.service.message.say

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class GraveSayDomainService : SayTypeDomainService {

    override fun isViewable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?,
        day: Int
    ): Boolean {
        return village.isViewableGraveSay(player) || myself?.isViewableGraveSay() ?: false
    }

    override fun isSayable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?
    ): Boolean {
        return village.isSayableGraveSay() && myself?.isSayableGraveSay() ?: false
    }
}
