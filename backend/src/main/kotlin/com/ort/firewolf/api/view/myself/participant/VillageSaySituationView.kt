package com.ort.firewolf.api.view.myself.participant

import com.ort.firewolf.domain.model.charachip.CharaFace
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.MessageType
import com.ort.firewolf.domain.model.myself.participant.VillageSaySituation
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village

data class VillageSaySituationView(
    val availableSay: Boolean,
    val selectableMessageTypeList: List<VillageSayMessageTypeSituationView>,
    val selectableFaceTypeList: List<CharaFace>,
    val defaultMessageType: MessageType?
) {

    constructor(
        situation: VillageSaySituation,
        village: Village,
        players: Players,
        charas: Charas,
        shouldHidePlayer: Boolean
    ) : this(
        availableSay = situation.isAvailableSay,
        selectableMessageTypeList = situation.selectableMessageTypeList.map {
            VillageSayMessageTypeSituationView(
                situation = it,
                village = village,
                players = players,
                charas = charas,
                shouldHidePlayer = shouldHidePlayer
            )
        },
        selectableFaceTypeList = situation.selectableFaceTypeList,
        defaultMessageType = situation.defaultMessageType
    )
}
