package com.ort.firewolf.domain.model.myself.participant

data class VillageCreatorSituation(
    val isAvailableCreatorSetting: Boolean,
    val isAvailableCreatorSay: Boolean,
    val isAvailableCancelVillage: Boolean,
    val isAvailableKick: Boolean,
    val isAvailableModifySetting: Boolean,
    val isAvailableExtendEpilogue: Boolean
)
