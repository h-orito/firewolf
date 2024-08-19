package com.ort.firewolf.domain.model.village.ability

import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageDay
import com.ort.firewolf.domain.model.village.participant.VillageParticipant

data class VillageAbilities(
    val list: List<VillageAbility>
) {

    fun filterLatestday(village: Village): VillageAbilities = filterByDay(village.day.latestDay())

    fun filterPastDay(villageDay: VillageDay): VillageAbilities =
        copy(list = list.filter { it.villageDayId < villageDay.id })

    fun filterYesterday(village: Village): VillageAbilities = filterByDay(village.day.yesterday())

    fun filterByType(abilityType: AbilityType): VillageAbilities {
        return this.copy(
            list = list.filter { it.abilityType.code == abilityType.code }
        )
    }

    fun filterByParticipantId(participantId: Int): VillageAbilities =
        copy(list = list.filter { it.myselfId == participantId })

    fun findYesterday(
        village: Village,
        participant: VillageParticipant,
        type: AbilityType
    ): VillageAbility? {
        return filterByDay(village.day.yesterday())
            .filterByParticipantId(participant.id)
            .filterByType(type)
            .list.firstOrNull()
    }

    fun existsDifference(abilities: VillageAbilities): Boolean {
        return list.size != abilities.list.size
    }

    fun add(ability: VillageAbility): VillageAbilities {
        return this.copy(list = list + ability)
    }

    fun addAll(abilityList: List<VillageAbility>): VillageAbilities {
        if (abilityList.isEmpty()) return this
        return this.copy(list = list + abilityList)
    }

    private fun filterByDay(villageDay: VillageDay): VillageAbilities {
        return this.copy(
            list = list.filter { it.villageDayId == villageDay.id }
        )
    }
}