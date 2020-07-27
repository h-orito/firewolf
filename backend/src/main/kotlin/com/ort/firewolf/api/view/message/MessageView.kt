package com.ort.firewolf.api.view.message

import com.ort.firewolf.api.view.village.VillageParticipantView
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village

data class MessageView(
    val from: VillageParticipantView?,
    val to: VillageParticipantView?,
    val time: MessageTimeView,
    val content: MessageContent
) {
    constructor(
        message: Message,
        village: Village,
        players: Players,
        charas: Charas,
        shouldHidePlayer: Boolean
    ) : this(
        from = if (message.fromVillageParticipantId == null) null else VillageParticipantView(
            village,
            message.fromVillageParticipantId,
            players,
            charas,
            shouldHidePlayer
        ),
        to = if (message.toVillageParticipantId == null) null else VillageParticipantView(
            village,
            message.toVillageParticipantId,
            players,
            charas,
            shouldHidePlayer
        ),
        time = MessageTimeView(message.time, village.day.dayList.first { it.id == message.time.villageDayId }),
        content = message.content
    )
}