package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipants

data class VillageParticipantsView(
    val count: Int,
    val memberList: List<VillageParticipantView>
) {
    constructor(
        village: Village,
        participants: VillageParticipants,
        charas: Charas,
        players: Players,
        shouldHidePlayer: Boolean
    ) : this(
        count = participants.count,
        memberList = participants.memberList.map {
            VillageParticipantView(
                village = village,
                villageParticipantId = it.id,
                players = players,
                charas = charas,
                shouldHidePlayer = shouldHidePlayer
            )
        }
    )
}
