package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

interface IAbilityDomainService {

    fun getAbilityType(): AbilityType

    fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities
    ): List<VillageParticipant>

    fun getSelectingTarget(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities
    ): VillageParticipant?

    fun createSetMessage(
        myself: VillageParticipant,
        target: VillageParticipant?
    ): String

    fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility>

    fun processDayChangeAction(dayChange: DayChange): DayChange

    fun isAvailableNoTarget(village: Village): Boolean

    fun isUsable(
        village: Village,
        participant: VillageParticipant
    ): Boolean
}