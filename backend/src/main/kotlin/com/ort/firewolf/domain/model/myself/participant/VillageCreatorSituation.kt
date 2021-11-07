package com.ort.firewolf.domain.model.myself.participant

data class VillageCreatorSituation(
    val availableCreatorSetting: Boolean,
    val availableCreatorSay: Boolean,
    val availableCancelVillage: Boolean,
    val availableKick: Boolean,
    val availableModifySetting: Boolean,
    val availableExtendEpilogue: Boolean
)
