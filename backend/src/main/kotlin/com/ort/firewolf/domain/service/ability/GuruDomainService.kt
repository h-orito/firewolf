package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class GuruDomainService {

    fun processDayChangeAction(dayChange: DayChange, charas: Charas): DayChange {
        // 導師がいない、または処刑・突然死がいない場合は何もしない
        val existsAliveGuru = dayChange.village.participant.filterAlive().memberList.any { it.skill!!.toCdef().isHasGuruPsychicAbility }
        if (!existsAliveGuru) return dayChange
        val todayDeadParticipants = dayChange.village.todayDeadParticipants().memberList.filter { it.dead!!.toCdef().isPsychicableDeath }
        if (todayDeadParticipants.isEmpty()) return dayChange

        var messages = dayChange.messages.copy()
        todayDeadParticipants.forEach { deadParticipant ->
            messages = messages.add(createGuruPrivateMessage(dayChange.village, charas, deadParticipant))
        }
        return dayChange.copy(messages = messages).setIsChange(dayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun createGuruPrivateMessage(
        village: Village,
        charas: Charas,
        deadParticipant: VillageParticipant
    ): Message {
        val chara = charas.chara(deadParticipant.charaId)
        val skill = village.participant.member(deadParticipant.id).skill!!.name
        val text = createGuruPrivateMessageString(chara, skill)
        return Message.createGuruPsychicPrivateMessage(text, village.day.latestDay().id)
    }

    private fun createGuruPrivateMessageString(chara: Chara, skill: String): String =
        "${chara.charaName.fullName()}は${skill}のようだ。"
}