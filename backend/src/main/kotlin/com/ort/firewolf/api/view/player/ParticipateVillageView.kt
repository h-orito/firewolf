package com.ort.firewolf.api.view.player

import com.ort.firewolf.api.view.village.VillageParticipantView
import com.ort.firewolf.api.view.village.VillageView
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

data class ParticipateVillageView(
    val village: VillageView,
    val participant: VillageParticipantView
) {

    constructor(
        village: Village,
        participant: VillageParticipant,
        charas: Charas,
        players: Players,
        createPlayer: Player
    ) : this(
        village = VillageView(village, charas, players, createPlayer),
        participant = VillageParticipantView(village, participant.id, players, charas, false)
    )
}
