package com.ort.firewolf.domain.model.village.participant

data class VillageParticipantName(
    val name: String,
    val shortName: String
) {
    fun existsDifference(other: VillageParticipantName): Boolean {
        return name != other.name || shortName != other.shortName
    }

    fun fullName(): String = "[$shortName] $name"
}