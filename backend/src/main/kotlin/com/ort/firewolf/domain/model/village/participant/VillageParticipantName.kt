package com.ort.firewolf.domain.model.village.participant

import com.fasterxml.jackson.annotation.JsonProperty

data class VillageParticipantName(
    val name: String,
    val shortName: String
) {
    fun existsDifference(other: VillageParticipantName): Boolean {
        return name != other.name || shortName != other.shortName
    }

    @JsonProperty("full_name")
    fun fullName(): String = "[$shortName] $name"
}