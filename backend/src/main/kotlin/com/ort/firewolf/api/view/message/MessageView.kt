package com.ort.firewolf.api.view.message

import com.ort.firewolf.api.view.village.VillageParticipantView
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipantName

data class MessageView(
    val from: VillageParticipantView?,
    val fromCharacterName: VillageParticipantName?,
    val to: VillageParticipantView?,
    val toCharacterName: VillageParticipantName?,
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
        fromCharacterName = message.fromCharacterName,
        to = if (message.toVillageParticipantId == null) null else VillageParticipantView(
            village,
            message.toVillageParticipantId,
            players,
            charas,
            shouldHidePlayer
        ),
        toCharacterName = message.toCharacterName,
        time = MessageTimeView(message.time, village.day.dayList.first { it.id == message.time.villageDayId }),
        content = message.content
    )
}