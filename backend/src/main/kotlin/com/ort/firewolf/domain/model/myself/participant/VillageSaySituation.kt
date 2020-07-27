package com.ort.firewolf.domain.model.myself.participant

import com.ort.firewolf.domain.model.charachip.CharaFace
import com.ort.firewolf.domain.model.message.MessageType

data class VillageSaySituation(
    val isAvailableSay: Boolean,
    val selectableMessageTypeList: List<VillageSayMessageTypeSituation> = listOf(),
    val selectableFaceTypeList: List<CharaFace>,
    val defaultMessageType: MessageType?
)
