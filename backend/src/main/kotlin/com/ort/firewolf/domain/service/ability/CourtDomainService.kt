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
class CourtDomainService : IAbilityDomainService {

    override fun getAbilityType(): AbilityType = AbilityType(CDef.AbilityType.求愛)

    override fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant,
        villageAbilities: VillageAbilities
    ): List<VillageParticipant> = getOnlyOneTimeAliveTargets(village, participant, villageAbilities, getAbilityType())

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
        return "${myself.name()}が求愛対象を${target?.name() ?: "なし"}に設定しました。"
    }

    override fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility> {
        // 進行中のみ
        if (!village.status.isProgress()) return listOf()
        // 最新日id
        val latestVillageDay = village.day.latestDay()
        // 1日目のみ
        if (latestVillageDay.day != 1) {
            return listOf()
        }

        // 生存している求愛能力持ちごとに
        return village
            .notDummyParticipant() // ダミーは求愛しない
            .filterAlive()
            .filterBySkill(CDef.Skill.求愛者.toModel()).memberList
            .mapNotNull { court ->
                // 対象はランダム
                getSelectableTargetList(village, court, villageAbilities).shuffled().firstOrNull()?.let {
                    VillageAbility(
                        villageDayId = latestVillageDay.id,
                        myselfId = court.id,
                        targetId = it.id,
                        abilityType = getAbilityType()
                    )
                }
            }
    }

    override fun processDayChangeAction(dayChange: DayChange): DayChange {
        var messages = dayChange.messages.copy()
        var village = dayChange.village

        village.participant
            .filterAlive()
            .filterBySkill(CDef.Skill.求愛者.toModel())
            .memberList.forEach { court ->
                val ability = dayChange.abilities.findYesterday(village, court, getAbilityType())
                    ?: return@forEach
                val target = village.participant.member(ability.targetId!!)
                // 相互恋絆を結ぶ
                village = village.courtParticipant(court.id, target.id)
                messages = messages.add(createCourtMessage(village, court, target))
                messages = messages.add(createCourtedMessage(village, court, target))
            }

        return dayChange.copy(
            messages = messages,
            village = village
        ).setIsChange(dayChange)
    }

    override fun isAvailableNoTarget(village: Village): Boolean = false

    override fun isUsable(village: Village, participant: VillageParticipant): Boolean {
        // 1日目のみ使用可能
        return participant.isAlive() && village.day.latestDay().day == 1
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createCourtMessage(village: Village, myself: VillageParticipant, target: VillageParticipant): Message {
        return Message.createPrivateLoversMessage(
            text = "${myself.name()}は、${target.name()}に求愛した。",
            villageParticipant = myself,
            villageDayId = village.day.latestDay().id,
        )
    }

    private fun createCourtedMessage(village: Village, court: VillageParticipant, myself: VillageParticipant): Message {
        return Message.createPrivateLoversMessage(
            text = "${myself.name()}は、${court.name()}に求愛された。",
            villageParticipant = myself,
            villageDayId = village.day.latestDay().id,
        )
    }
}