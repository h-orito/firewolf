package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class GuruDomainService {

    fun processDayChangeAction(dayChange: DayChange): DayChange {
        // 導師がいない、または処刑・突然死がいない場合は何もしない
        val existsAliveGuru =
            dayChange.village.participant.filterAlive().memberList.any { it.skill!!.toCdef().isHasGuruPsychicAbility }
        if (!existsAliveGuru) return dayChange
        val todayDeadParticipants =
            dayChange.village.todayDeadParticipants().memberList.filter { it.dead!!.toCdef().isPsychicableDeath }
        if (todayDeadParticipants.isEmpty()) return dayChange

        var messages = dayChange.messages.copy()
        todayDeadParticipants.forEach { deadParticipant ->
            messages = messages.add(createGuruPrivateMessage(dayChange.village, deadParticipant))
        }
        return dayChange.copy(messages = messages).setIsChange(dayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createGuruPrivateMessage(
        village: Village,
        deadParticipant: VillageParticipant
    ): Message {
        val skill = village.participant.member(deadParticipant.id).skill!!.name
        val text = createGuruPrivateMessageString(deadParticipant, skill)
        return Message.createGuruPsychicPrivateMessage(text, village.day.latestDay().id)
    }

    private fun createGuruPrivateMessageString(target: VillageParticipant, skill: String): String =
        "${target.name()}は${skill}のようだ。"
}