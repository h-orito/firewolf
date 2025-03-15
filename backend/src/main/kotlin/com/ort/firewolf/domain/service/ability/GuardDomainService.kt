package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class GuardDomainService : IAbilityDomainService {

    override fun getAbilityType(): AbilityType = AbilityType(CDef.AbilityType.護衛)

    override fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant,
        villageAbilities: VillageAbilities
    ): List<VillageParticipant> {
        // 1日目は護衛できない
        if (village.day.latestDay().day <= 1) return emptyList()

        // 連続護衛可能なら自分以外の生存者全員
        val base = village.participant
            .filterAlive()
            .filterNotParticipant(participant)
            .memberList
        if (village.setting.rules.availableGuardSameTarget) return base

        // 昨日の護衛先
        val yesterdayGuardTarget = villageAbilities.findYesterday(
            village = village,
            participant = participant,
            type = getAbilityType()
        ) ?: return base
        // 昨日の護衛先は護衛できない
        return base.filterNot { it.id == yesterdayGuardTarget.targetId }
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
            ?.targetId
        targetVillageParticipantId ?: return null
        return village.participant.member(targetVillageParticipantId)
    }

    override fun createSetMessage(myself: VillageParticipant, target: VillageParticipant?): String {
        return "${myself.name()}が護衛対象を${target?.name() ?: "なし"}に設定しました。"
    }

    override fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility> {
        // 進行中のみ
        if (!village.status.isProgress()) return listOf()

        // 生存している護衛能力持ちごとに
        return village.participant.filterAlive().memberList.filter {
            it.skill!!.toCdef().isHasGuardAbility
        }.mapNotNull { hunter ->
            getSelectableTargetList(village, hunter, villageAbilities).randomOrNull()?.let {
                VillageAbility(
                    villageDayId = village.day.latestDay().id,
                    myselfId = hunter.id,
                    targetId = it.id,
                    abilityType = getAbilityType()
                )
            }
        }
    }

    override fun processDayChangeAction(dayChange: DayChange): DayChange {
        var messages = dayChange.messages.copy()

        dayChange.village.participant.filterAlive().memberList
            .filter { it.skill!!.toCdef().isHasGuardAbility }
            .forEach { hunter ->
                dayChange.abilities.findYesterday(dayChange.village, hunter, getAbilityType())?.let { ability ->
                    messages = messages.add(createGuardMessage(dayChange.village, ability))
                }
            }

        return dayChange.copy(
            messages = messages
        ).setIsChange(dayChange)
    }

    override fun isAvailableNoTarget(village: Village): Boolean = false

    override fun isUsable(village: Village, participant: VillageParticipant): Boolean {
        // 2日目以降、生存していたら行使できる
        return village.day.latestDay().day > 1 && participant.isAlive()
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createGuardMessage(village: Village, ability: VillageAbility): Message {
        val hunter = village.participant.member(ability.myselfId)
        val target = village.participant.member(ability.targetId!!)
        val text = createGuardMessageString(hunter, target)
        return Message.createPrivateAbilityMessage(
            text = text,
            villageDayId = village.day.latestDay().id,
            villageParticipant = hunter
        )
    }

    private fun createGuardMessageString(hunter: VillageParticipant, target: VillageParticipant): String =
        "${hunter.name()}は、${target.name()}を護衛している。"
}