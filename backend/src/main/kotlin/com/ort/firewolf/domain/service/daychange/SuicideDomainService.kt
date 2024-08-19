package com.ort.firewolf.domain.service.daychange

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class SuicideDomainService {

    fun suicide(daychange: DayChange): DayChange {
        var village = daychange.village.copy()
        var messages = daychange.messages.copy()

        while (existsSuicideTarget(village)) {
            val target = findSuicideTarget(village)!!

            val loverSuicideTarget = findLoverSuicideTarget(village)
            messages = if (loverSuicideTarget != null) {
                val lover = target.getTargetLovers(village).filterDead().memberList.shuffled().first()
                messages.add(createLoverSuicideMessage(village, target, lover))
            } else {
                messages.add(createImmoralSuicideMessage(village, target))
            }
            village = village.suicideParticipant(target.id)
        }

        return daychange.copy(village = village, messages = messages)
    }

    private fun findSuicideTarget(village: Village): VillageParticipant? {
        // 恋絆
        findLoverSuicideTarget(village)?.let { return it }
        // 背徳者
        return findImmoralSuicideTarget(village)
    }

    private fun existsSuicideTarget(village: Village): Boolean = findSuicideTarget(village) != null

    private fun findLoverSuicideTarget(village: Village): VillageParticipant? {
        return village.participant.filterAlive().memberList // 自分は生きていて
            .filter { it.status.hasLover() } // 恋人がいて
            .firstOrNull { participant ->
                // 恋人のいずれかが死亡している
                participant.getTargetLovers(village).memberList.any { it.isDead() }
            }
    }

    private fun findImmoralSuicideTarget(village: Village): VillageParticipant? {
        // 妖狐系が生存していたら後追いしない
        if (village.participant.filterAlive().memberList.any { it.skill!!.isFoxCount() }) {
            return null
        }
        // 生存している背徳者が後追い対象
        return village.participant
            .filterAlive().memberList
            .firstOrNull { it.skill!!.camp().isFoxs() }
    }

    private fun createLoverSuicideMessage(
        village: Village,
        target: VillageParticipant,
        lover: VillageParticipant
    ): Message {
        return Message.createPublicSystemMessage(
            text = "${target.name()}は、絆に引きずられるように${lover.name()}の後を追った。",
            villageDayId = village.day.latestDay().id,
        )
    }

    private fun createImmoralSuicideMessage(village: Village, target: VillageParticipant): Message {
        return Message.createPublicSystemMessage(
            text = "${target.name()}は、妖狐の後を追い、いなくなってしまった。",
            villageDayId = village.day.latestDay().id
        )
    }
}
