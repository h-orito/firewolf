package com.ort.firewolf.domain.model.myself.participant

import com.ort.firewolf.domain.model.village.participant.VillageParticipant

data class VillageVoteSituation(
    val isAvailableVote: Boolean,
    val targetList: List<VillageParticipant>,
    val target: VillageParticipant?
)