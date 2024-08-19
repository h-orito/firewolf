package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageDay
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import org.springframework.stereotype.Service

@Service
class AttackDomainService : IAbilityDomainService {

    override fun getAbilityType(): AbilityType = AbilityType(CDef.AbilityType.襲撃)

    override fun getSelectableTargetList(
        village: Village,
        participant: VillageParticipant,
        villageAbilities: VillageAbilities
    ): List<VillageParticipant> {
        return if (village.day.latestDay().day == 1) {
            // ダミーキャラ固定
            listOf(village.dummyChara())
        } else {
            // 襲撃対象に選べる & 生存している
            village.participant.filterAlive().memberList.filter {
                !it.skill!!.toCdef().isNotSelectableAttack
            }
        }
    }

    override fun getSelectingTarget(
        village: Village,
        participant: VillageParticipant?,
        villageAbilities: VillageAbilities
    ): VillageParticipant? {
        participant ?: return null

        // 襲撃能力のある参加者のID
        val attackableParticipantIdList =
            village.participant.memberList.filter { it.skill!!.hasAttackAbility() }.map { it.id }

        val targetVillageParticipantId = villageAbilities
            .filterLatestday(village)
            .filterByType(getAbilityType()).list
            .find { attackableParticipantIdList.contains(it.myselfId) }
            ?.targetId
        targetVillageParticipantId ?: return null
        return village.participant.member(targetVillageParticipantId)
    }

    fun getSelectingAttacker(
        village: Village,
        villageAbilities: VillageAbilities
    ): VillageParticipant? {
        // 襲撃能力のある参加者のID
        val attackableParticipantIdList =
            village.participant.memberList.filter { it.skill!!.hasAttackAbility() }.map { it.id }

        val attackerId = villageAbilities
            .filterLatestday(village)
            .filterByType(getAbilityType()).list
            .find { attackableParticipantIdList.contains(it.myselfId) }
            ?.myselfId ?: return null
        return village.participant.member(attackerId)
    }

    fun getSelectableAttacker(village: Village): List<VillageParticipant> {
        return village.participant.filterAlive().memberList.filter { it.skill!!.hasAttackAbility() }
    }

    override fun createSetMessage(myself: VillageParticipant, target: VillageParticipant?): String =
        "襲撃者を${myself.name()}に、襲撃対象を${target?.name() ?: "なし"}に設定しました。"

    override fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility> {
        // 進行中のみ
        if (!village.status.isProgress()) return listOf()
        // 襲撃者は生存している人狼からランダムに
        val wolf = village.participant.filterAlive().findRandom {
            it.skill!!.toCdef().isHasAttackAbility
        } ?: return listOf() // 生存している人狼がいないので襲撃なし
        // 対象も選択可能なものからランダム
        return getSelectableTargetList(village, wolf, villageAbilities)
            .shuffled().firstOrNull()
            ?.let {
                listOf(
                    VillageAbility(
                        villageDayId = village.day.latestDay().id,
                        myselfId = wolf.id,
                        targetId = it.id,
                        abilityType = getAbilityType()
                    )
                )
            } ?: return listOf()
    }

    override fun processDayChangeAction(
        dayChange: DayChange,
    ): DayChange {
        val latestDay = dayChange.village.day.latestDay()
        // 人狼が全員死亡していたら襲撃なし
        if (dayChange.village.participant.filterAlive().memberList.none { it.skill!!.hasAttackAbility() }) {
            return dayChange
        }

        var village = dayChange.village.copy()
        var messages = dayChange.messages.copy()
        dayChange.abilities
            .filterByType(getAbilityType())
            .filterYesterday(village).list
            .find { it.targetId != null }
            ?.let { ability ->
                val attacker = village.participant.member(ability.myselfId)
                // 襲撃メッセージ
                messages = messages.add(createAttackMessage(village, attacker, ability))
                // 襲撃成功したら死亡
                if (isAttackSuccess(dayChange, ability.targetId!!)) {
                    village = village.attackParticipant(ability.targetId, latestDay)
                    val target = village.participant.member(ability.targetId)

                    // 智狼がいれば追加メッセージ
                    createWiseWolfMessage(village, target)?.let {
                        messages = messages.add(it)
                    }
                    // 猫又による道連れ
                    forceSuicidedParticipant(target, attacker, village.participant)?.let {
                        village = village.divineKillParticipant(it.id, village.day.latestDay())
                        messages = messages.add(
                            createForceSuicideMessage(
                                village.participant.member(ability.targetId),
                                it,
                                village.day.latestDay(),
                            )
                        )
                    }
                }
            } ?: return dayChange

        return dayChange.copy(
            village = village,
            messages = messages
        ).setIsChange(dayChange)
    }

    override fun isAvailableNoTarget(village: Village): Boolean = village.day.latestDay().day != 1 // 1日目はダミー固定

    override fun isUsable(village: Village, participant: VillageParticipant): Boolean {
        // 生存していたら行使できる
        return participant.isAlive()
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isAttackSuccess(dayChange: DayChange, targetId: Int): Boolean {
        // 対象が既に死亡していたら失敗
        if (!dayChange.village.participant.member(targetId).isAlive()) return false
        // 対象が護衛されていたら失敗
        if (dayChange.abilities.list.any { villageAbility ->
                (villageAbility.abilityType.code == CDef.AbilityType.護衛.code()
                        || villageAbility.abilityType.code == CDef.AbilityType.風来護衛.code())
                        && villageAbility.targetId == targetId
                        && villageAbility.villageDayId == dayChange.village.day.yesterday().id
                        && dayChange.village.participant.member(villageAbility.myselfId).isAlive()
            }) {
            return false
        }
        // 対象が襲撃を耐える役職なら失敗
        return !dayChange.village.participant.member(targetId).skill!!.toCdef().isNoDeadByAttack
    }

    private fun createAttackMessage(
        village: Village,
        wolf: VillageParticipant,
        ability: VillageAbility
    ): Message {
        val text = createAttackMessageString(wolf, village.participant.member(ability.targetId!!))
        return Message.createAttackPrivateMessage(text, village.day.latestDay().id)
    }

    private fun createAttackMessageString(attacker: VillageParticipant, target: VillageParticipant): String =
        "${attacker.name()}達は、${target.name()}を襲撃した。"

    private fun createWiseWolfMessage(village: Village, target: VillageParticipant): Message? {
        // 智狼がいなければ何もしない
        if (!village.participant.filterAlive().memberList.any { it.skill!!.toCdef().isHasWiseWolfAbility }) return null
        // 対象の役職を知られる
        val skill = target.skill!!.name
        return Message.createAttackPrivateMessage(
            "${target.name()}は${skill}だったようだ。",
            village.day.latestDay().id
        )
    }

    private fun forceSuicidedParticipant(
        attackedParticipant: VillageParticipant,
        attacker: VillageParticipant,
        participants: VillageParticipants
    ): VillageParticipant? {
        // 襲撃されたのが道連れ役職でなければ何もしない
        if (!attackedParticipant.skill!!.toCdef().isForceDoubleSuicide) return null
        // 襲撃者が死亡している場合、生存している他の人狼をランダムで道連れにする
        return if (attacker.isDead()) {
            participants
                .filterAlive().memberList
                .filter { it.skill!!.toCdef().isHasAttackAbility }
                .filterNot { it.id == attacker.id }
                .randomOrNull()
        } else attacker
    }

    private fun createForceSuicideMessage(
        attackedParticipant: VillageParticipant,
        forceSuicidedParticipant: VillageParticipant,
        latestDay: VillageDay,
    ): Message {
        val attackedCharaName = attackedParticipant.name()
        val forceSuicidedCharaName = forceSuicidedParticipant.name()
        val message = "${attackedCharaName}は、${forceSuicidedCharaName}を道連れにした。"
        return Message.createPrivateSystemMessage(
            message,
            latestDay.id
        )
    }
}