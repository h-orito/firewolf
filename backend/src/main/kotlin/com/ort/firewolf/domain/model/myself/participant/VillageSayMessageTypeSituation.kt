package com.ort.firewolf.domain.model.myself.participant

import com.ort.firewolf.domain.model.message.MessageType
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

data class VillageSayMessageTypeSituation(
    val messageType: MessageType,
    val restrict: VillageSayRestrictSituation,
    // 秘話用
    val targetList: List<VillageParticipant>
)