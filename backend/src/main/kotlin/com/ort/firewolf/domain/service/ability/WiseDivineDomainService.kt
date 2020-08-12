package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class WiseDivineDomainService : DivineDomainService() {

    override fun processDayChangeAction(dayChange: DayChange, charas: Charas): DayChange {
        val latestDay = dayChange.village.day.latestDay()
        var messages = dayChange.messages.copy()
        var village = dayChange.village.copy()

        dayChange.village.participant.memberList.filter {
            it.isAlive() && it.skill!!.toCdef().isHasWiseDivineAbility
        }.forEach { wise ->
            dayChange.abilities.filterYesterday(village).list.find {
                it.myselfId == wise.id
            }?.let { ability ->
                messages = messages.add(createWiseDivineMessage(dayChange.village, charas, ability, wise))
                // 呪殺対象なら死亡
                if (isDivineKill(dayChange, ability.targetId!!)) village = village.divineKillParticipant(ability.targetId, latestDay)
                // 逆呪殺対象なら自分が死亡
                if (isCounterDivineKill(dayChange, ability.targetId)) village = village.divineKillParticipant(wise.id, latestDay)
            }
        }

        return dayChange.copy(
            messages = messages,
            village = village
        ).setIsChange(dayChange)
    }

    override fun getDefaultAbilityList(
        village: Village,
        villageAbilities: VillageAbilities
    ): List<VillageAbility> {
        // 進行中のみ
        if (!village.status.isProgress()) return listOf()
        // 生存している役職占い能力持ちごとに
        return village.participant.filterAlive().memberList.filter {
            it.skill!!.toCdef().isHasWiseDivineAbility
        }.mapNotNull { seer ->
            // 対象は自分以外の生存者からランダム
            village.participant
                .filterAlive()
                .findRandom { it.id != seer.id }
                ?.let {
                    VillageAbility(
                        villageDayId = village.day.latestDay().id,
                        myselfId = seer.id,
                        targetId = it.id,
                        abilityType = getAbilityType()
                    )
                }
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createWiseDivineMessage(
        village: Village,
        charas: Charas,
        ability: VillageAbility,
        wise: VillageParticipant
    ): Message {
        val myself = village.participant.member(ability.myselfId)
        val myChara = charas.chara(myself.charaId)
        val targetChara = charas.chara(village.participant, ability.targetId!!)
        val skill = village.participant.member(ability.targetId).skill!!.name
        val text = createDivineMessageString(myChara, targetChara, skill)
        return Message.createWisePrivateMessage(text, village.day.latestDay().id, wise)
    }

    private fun createDivineMessageString(chara: Chara, targetChara: Chara, skill: String): String =
        "${chara.charaName.fullName()}は、${targetChara.charaName.fullName()}を占った。\n${targetChara.charaName.fullName()}は${skill}のようだ。"

    private fun isDivineKill(dayChange: DayChange, targetId: Int): Boolean {
        // 対象が既に死亡していたら呪殺ではない
        if (!dayChange.village.participant.member(targetId).isAlive()) return false
        // 対象が呪殺対象でなければ呪殺ではない
        return dayChange.village.participant.member(targetId).skill!!.toCdef().isDeadByDivine
    }

    private fun isCounterDivineKill(dayChange: DayChange, targetId: Int): Boolean {
        // 対象が既に死亡していたら呪殺ではない
        if (!dayChange.village.participant.member(targetId).isAlive()) return false
        // 対象が逆呪殺対象でなければ逆呪殺されない
        return dayChange.village.participant.member(targetId).skill!!.toCdef().isCounterDeadByDivine
    }
}