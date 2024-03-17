package com.ort.firewolf.domain.service.message.say

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class WerewolfSayDomainService : SayTypeDomainService {

    override fun isViewable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?,
        day: Int
    ): Boolean =
        village.isViewableWerewolfSay(player) || myself?.isViewableWerewolfSay() ?: false

    override fun isSayable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?
    ): Boolean =
        village.isSayableWerewolfSay() && myself?.isSayableWerewolfSay() ?: false
}
