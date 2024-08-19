package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.ability.AbilityTypes
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.myself.participant.VillageAbilitySituation
import com.ort.firewolf.domain.model.myself.participant.VillageAbilitySituations
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBadRequestException
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class AbilityDomainService(
    private val attackDomainService: AttackDomainService,
    private val divineDomainService: DivineDomainService,
    private val wiseDivineDomainService: WiseDivineDomainService,
    private val guardDomainService: GuardDomainService,
    private val wandererGuardDomainService: WandererGuardDomainService,
    private val courtDomainService: CourtDomainService,
    private val hiyashichukaDomainService: HiyashichukaDomainService
) {

    // 選択可能な対象
    fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities,
        abilityType: AbilityType
    ): List<VillageParticipant> {
        if (!canUseAbility(village, participant)) return listOf()
        return detectDomainService(abilityType)?.getSelectableTargetList(village, participant!!, villageAbilities)
            ?: listOf()
    }

    // 選択中の対象
    fun getSelectingTarget(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities,
        abilityType: AbilityType
    ): VillageParticipant? {
        if (!canUseAbility(village, participant)) return null
        return detectDomainService(abilityType)?.getSelectingTarget(village, participant, villageAbilities)
    }

    // 能力セットメッセージ
    fun createAbilitySetMessage(
        village: Village,
        myself: VillageParticipant,
        target: VillageParticipant?,
        abilityType: AbilityType
    ): Message {
        return detectDomainService(abilityType)?.createSetMessage(myself, target)?.let {
            Message.createPrivateSystemMessage(it, village.day.latestDay().id)
        } ?: throw IllegalStateException("想定外の能力")
    }

    fun assertAbility(
        village: Village,
        participant: VillageParticipant?,
        myselfId: Int?,
        targetId: Int?,
        abilityType: AbilityType,
        villageAbilities: VillageAbilities
    ) {
        participant?.skill ?: throw FirewolfBadRequestException("役職なし")
        // その能力を持っていない
        if (AbilityTypes(participant.skill).list.none { it.code == abilityType.code }) {
            throw FirewolfBadRequestException("${abilityType.name}の能力を持っていません")
        }
        // 使えない状況
        if (!isUsable(village, participant, abilityType)) {
            throw FirewolfBusinessException("${abilityType.name}能力を使えない状態です")
        }
        // 対象指定がおかしい
        if (abilityType.toCdef() == CDef.AbilityType.襲撃 && myselfId == null) throw FirewolfBusinessException("襲撃者指定が必要です")
        if (targetId == null && !canNoTarget(village, abilityType)) throw FirewolfBusinessException("対象指定が必要です")
        if (targetId != null && getSelectableTargetList(
                village,
                participant,
                villageAbilities,
                abilityType
            ).none { it.id == targetId }
        ) {
            throw FirewolfBusinessException("指定できない対象を指定しています")
        }
    }

    fun isUsable(
        village: Village,
        participant: VillageParticipant?,
        abilityType: AbilityType
    ): Boolean {
        participant ?: return false
        // 進行中でないと使えない
        if (!village.status.isProgress()) return false
        return detectDomainService(abilityType)?.isUsable(village, participant) ?: false
    }

    fun canNoTarget(village: Village, abilityType: AbilityType): Boolean =
        detectDomainService(abilityType)?.isAvailableNoTarget(village) ?: false

    fun convertToSituationList(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities
    ): VillageAbilitySituations {
        participant?.skill ?: return VillageAbilitySituations(listOf())
        val abilityTypes = AbilityTypes(participant.skill)
        return VillageAbilitySituations(
            list = abilityTypes.list.map { convertToSituation(village, participant, it, villageAbilities) }
        )
    }

    // 日付更新時のデフォルト能力行使を追加
    fun addDefaultAbilities(dayChange: DayChange): DayChange {
        val village = dayChange.village
        var abilities = dayChange.abilities

        // 襲撃
        abilities = abilities.addAll(attackDomainService.getDefaultAbilityList(village, abilities))
        // 占い
        abilities = abilities.addAll(divineDomainService.getDefaultAbilityList(village, abilities))
        abilities = abilities.addAll(wiseDivineDomainService.getDefaultAbilityList(village, abilities))
        // 護衛
        abilities = abilities.addAll(guardDomainService.getDefaultAbilityList(village, abilities))
        // 求愛
        abilities = abilities.addAll(courtDomainService.getDefaultAbilityList(village, abilities))

        return dayChange.copy(abilities = abilities).setIsChange(dayChange)
    }

    fun addRecongnizeMessages(orgDayChange: DayChange): DayChange {
        // 人狼系役職メッセージ追加
        var dayChange = addWolfsConfirmMessage(orgDayChange)
        // 狂信者がいれば狂信者向けメッセージ追加
        dayChange = addFanaticMessageIfNeeded(dayChange)
        // 共有がいれば役職メッセージ追加
        dayChange = addMasonsConfirmMessageIfNeeded(dayChange)
        // 共鳴がいれば役職メッセージ追加
        dayChange = addSympathizersConfirmMessageIfNeeded(dayChange)
        // 妖狐系がいれば役職メッセージ追加
        dayChange = addFoxsConfirmMessageIfNeeded(dayChange)

        return dayChange
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun detectDomainService(abilityType: AbilityType): IAbilityDomainService? {
        return when (abilityType.code) {
            CDef.AbilityType.襲撃.code() -> attackDomainService
            CDef.AbilityType.占い.code() -> divineDomainService
            CDef.AbilityType.護衛.code() -> guardDomainService
            CDef.AbilityType.風来護衛.code() -> wandererGuardDomainService
            CDef.AbilityType.求愛.code() -> courtDomainService
            CDef.AbilityType.冷やし中華.code() -> hiyashichukaDomainService
            else -> null
        }
    }

    private fun canUseAbility(village: Village, participant: VillageParticipant?): Boolean {
        // 村として可能か
        if (!village.canUseAbility()) return false
        // 参加者として可能か
        participant ?: return false
        return participant.canUseAbility()
    }

    private fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        abilityType: AbilityType,
        villageAbilities: VillageAbilities
    ): VillageAbilitySituation {
        return VillageAbilitySituation(
            type = abilityType,
            attacker = getSelectingAttacker(village, participant, villageAbilities, abilityType),
            attackerList = getSelectableAttacker(village, participant, abilityType),
            targetList = this.getSelectableTargetList(village, participant, villageAbilities, abilityType),
            target = this.getSelectingTarget(village, participant, villageAbilities, abilityType),
            usable = this.isUsable(village, participant, abilityType),
            isAvailableNoTarget = this.canNoTarget(village, abilityType)
        )
    }

    private fun addWolfsConfirmMessage(dayChange: DayChange): DayChange {
        return dayChange.copy(
            messages = dayChange.messages.add(dayChange.village.createWolfsConfirmMessage())
        )
    }

    private fun addFanaticMessageIfNeeded(dayChange: DayChange): DayChange {
        return dayChange.village.createFanaticConfirmMessage()?.let {
            dayChange.copy(messages = dayChange.messages.add(it))
        } ?: dayChange
    }

    private fun addMasonsConfirmMessageIfNeeded(dayChange: DayChange): DayChange {
        return dayChange.village.createMasonsConfirmMessage()?.let {
            dayChange.copy(messages = dayChange.messages.add(it))
        } ?: dayChange
    }

    private fun addSympathizersConfirmMessageIfNeeded(dayChange: DayChange): DayChange {
        return dayChange.village.createSympathizersConfirmMessage()?.let {
            dayChange.copy(messages = dayChange.messages.add(it))
        } ?: dayChange
    }

    private fun addFoxsConfirmMessageIfNeeded(dayChange: DayChange): DayChange {
        return dayChange.village.createFoxsConfirmMessage()?.let {
            dayChange.copy(messages = dayChange.messages.add(it))
        } ?: dayChange
    }

    private fun getSelectingAttacker(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities,
        abilityType: AbilityType
    ): VillageParticipant? {
        return if (!canUseAbility(village, participant)) null
        else if (abilityType.toCdef() != CDef.AbilityType.襲撃) null
        else attackDomainService.getSelectingAttacker(village, villageAbilities)
    }

    private fun getSelectableAttacker(
        village: Village,
        participant: VillageParticipant?,
        abilityType: AbilityType
    ): List<VillageParticipant> {
        return if (!canUseAbility(village, participant)) emptyList()
        else if (abilityType.toCdef() != CDef.AbilityType.襲撃) emptyList()
        else attackDomainService.getSelectableAttacker(village)
    }
}