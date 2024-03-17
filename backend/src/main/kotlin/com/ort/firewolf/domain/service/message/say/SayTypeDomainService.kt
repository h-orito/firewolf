package com.ort.firewolf.domain.service.message.say

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.service.message.MessageTypeDomainService

interface SayTypeDomainService : MessageTypeDomainService {

    fun isSayable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?
    ): Boolean
}