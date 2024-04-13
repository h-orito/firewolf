package com.ort.firewolf.domain.model.myself.participant

data class SituationAsParticipant(
    val participate: VillageParticipateSituation,
    val skillRequest: VillageSkillRequestSituation,
    val commit: VillageCommitSituation,
    val comingOut: VillageComingOutSituation,
    val say: VillageSaySituation,
    val rp: ParticipantRpSituation,
    val ability: VillageAbilitySituations,
    val vote: VillageVoteSituation,
    val creator: VillageCreatorSituation,
    val admin: VillageAdminSituation
)