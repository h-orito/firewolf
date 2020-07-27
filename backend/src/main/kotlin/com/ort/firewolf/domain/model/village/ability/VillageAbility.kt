package com.ort.firewolf.domain.model.village.ability

import com.ort.firewolf.domain.model.ability.AbilityType

data class VillageAbility(
    val villageDayId: Int,
    val myselfId: Int,
    val targetId: Int?,
    val abilityType: AbilityType
) {
}