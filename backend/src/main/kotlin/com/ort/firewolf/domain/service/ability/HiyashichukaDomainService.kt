package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.skill.toModel
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class HiyashichukaDomainService : IAbilityDomainService {

    override fun getAbilityType(): AbilityType = AbilityType(CDef.AbilityType.冷やし中華)

    override fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant,
        villageAbilities: VillageAbilities
    ): List<VillageParticipant> {
        return if (hasAlreadyUseAbility(village, participant, villageAbilities, getAbilityType())) emptyList()
        else listOf(participant)
    }

    override fun getSelectingTarget(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities
    ): VillageParticipant? {
        participant ?: return null

        val targetVillageParticipantId = villageAbilities
            .filterLatestday(village)
            .filterByType(getAbilityType()).list
            .find { it.myselfId == participant.id }
            ?.targetId ?: return null
        return village.participant.member(targetVillageParticipantId)
    }

    override fun createSetMessage(myself: VillageParticipant, target: VillageParticipant?): String {
        return if (target == null) "${myself.name()}が始まるのをやめました。"
        else "${myself.name()}が始まることにしました。"
    }

    override fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility> {
        return emptyList()
    }

    override fun processDayChangeAction(dayChange: DayChange): DayChange {
        var messages = dayChange.messages.copy()
        val village = dayChange.village

        village.participant
            .filterAlive()
            .filterBySkill(CDef.Skill.冷やし中華.toModel())
            .memberList.forEach { hiyashi ->
                dayChange.abilities.findYesterday(village, hiyashi, getAbilityType())
                    ?: return@forEach
                messages = messages.add(createMessage(village, hiyashi))
            }

        return dayChange.copy(
            messages = messages,
        ).setIsChange(dayChange)
    }

    override fun isAvailableNoTarget(village: Village): Boolean = true

    override fun isUsable(village: Village, participant: VillageParticipant): Boolean {
        return participant.isAlive()
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createMessage(village: Village, myself: VillageParticipant): Message {
        return Message.createPublicSystemMessage(
            text = "${myself.name()}は、始まった。",
            villageDayId = village.day.latestDay().id,
        )
    }
}