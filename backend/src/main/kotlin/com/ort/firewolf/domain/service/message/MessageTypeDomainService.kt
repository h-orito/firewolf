package com.ort.firewolf.domain.service.message

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

interface MessageTypeDomainService {

    fun isViewable(village: Village, myself: VillageParticipant?, player: Player?, day: Int): Boolean
}