package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

internal fun getOnlyOneTimeAliveTargets(
    village: Village,
    myself: VillageParticipant,
    abilities: VillageAbilities,
    abilityType: AbilityType
): List<VillageParticipant> {
    // 一度使うと使えない
    return if (hasAlreadyUseAbility(village, myself, abilities, abilityType)) emptyList()
    else village.notDummyParticipant()
        .filterAlive()
        .filterNotParticipant(myself)
        .memberList
}

internal fun hasAlreadyUseAbility(
    village: Village,
    myself: VillageParticipant,
    abilities: VillageAbilities,
    abilityType: AbilityType
): Boolean = abilities
    .filterByType(abilityType)
    .filterByParticipantId(myself.id)
    .filterPastDay(village.day.latestDay())
    .list.isNotEmpty()
