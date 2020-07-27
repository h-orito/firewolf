package com.ort.firewolf.api.view.village

import com.ort.firewolf.api.view.message.MessageView
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village

data class VillageAnchorMessageView(
    val message: MessageView?
) {
    constructor(
        message: Message?,
        village: Village,
        players: Players,
        charas: Charas
    ) : this(
        message = if (message == null) null else MessageView(
            message = message,
            village = village,
            players = players,
            charas = charas,
            shouldHidePlayer = !village.status.isSolved()
        )
    )
}