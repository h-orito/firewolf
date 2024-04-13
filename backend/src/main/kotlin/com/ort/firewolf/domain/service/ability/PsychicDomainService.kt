package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class PsychicDomainService {

    fun processDayChangeAction(dayChange: DayChange): DayChange {
        // 霊能がいない、または処刑・突然死がいない場合は何もしない
        val existsAlivePsychic =
            dayChange.village.participant.filterAlive().memberList.any { it.skill!!.toCdef().isHasPsychicAbility }
        if (!existsAlivePsychic) return dayChange
        val todayDeadParticipants =
            dayChange.village.todayDeadParticipants().memberList.filter { it.dead!!.toCdef().isPsychicableDeath }
        if (todayDeadParticipants.isEmpty()) return dayChange

        var messages = dayChange.messages.copy()
        todayDeadParticipants.forEach { deadParticipant ->
            messages = messages.add(createPsychicPrivateMessage(dayChange.village, deadParticipant))
        }
        return dayChange.copy(messages = messages).setIsChange(dayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createPsychicPrivateMessage(
        village: Village,
        deadParticipant: VillageParticipant
    ): Message {
        val isWolf = village.participant.member(deadParticipant.id).skill!!.toCdef().isPsychicResultWolf
        val text = createPsychicPrivateMessageString(deadParticipant, isWolf)
        return Message.createPsychicPrivateMessage(text, village.day.latestDay().id)
    }

    private fun createPsychicPrivateMessageString(target: VillageParticipant, isWolf: Boolean): String =
        "${target.name()}は人狼${if (isWolf) "の" else "ではない"}ようだ。"
}