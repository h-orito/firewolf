package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village

data class VillageParticipantsView(
    val count: Int,
    val memberList: List<VillageParticipantView>
) {
    constructor(
        village: Village,
        charas: Charas,
        players: Players,
        shouldHidePlayer: Boolean
    ) : this(
        count = village.participant.count,
        memberList = village.participant.memberList.map {
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
