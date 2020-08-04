package com.ort.firewolf.domain.model.player.record

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

data class ParticipateVillage(
    val village: Village,
    val participant: VillageParticipant
) {
    constructor(
        player: Player,
        village: Village
    ) : this(
        village = village,
        participant = village.findMemberByPlayerId(player.id)!!
    )
}