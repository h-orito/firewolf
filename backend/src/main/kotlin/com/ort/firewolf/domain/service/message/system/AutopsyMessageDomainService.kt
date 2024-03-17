package com.ort.firewolf.domain.service.message.system

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.service.message.MessageTypeDomainService
import org.springframework.stereotype.Service

@Service
class AutopsyMessageDomainService : MessageTypeDomainService {

    override fun isViewable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?,
        day: Int
    ): Boolean = village.isViewableAutopsyMessage() || myself?.isViewableAutopsyMessage() ?: false
}