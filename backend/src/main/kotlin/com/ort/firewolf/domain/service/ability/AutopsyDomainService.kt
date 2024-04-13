package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class AutopsyDomainService {

    fun addAutopsyMessage(dayChange: DayChange): DayChange {
        // 検死官がいなければ何もしない
        val existsCoroner = dayChange.village.participant.filterAlive().memberList.any {
            it.skill!!.toCdef().isHasAutopsyAbility
        }
        if (!existsCoroner) return dayChange

        // 無惨
        val todayMiserableDeathParticipantList = dayChange.village.todayDeadParticipants().memberList.filter {
            it.dead!!.toCdef().isMiserableDeath
        }
        if (todayMiserableDeathParticipantList.isEmpty()) return dayChange

        var messages = dayChange.messages.copy()
        todayMiserableDeathParticipantList.forEach { participant ->
            messages = messages.add(createAutopsyMessage(dayChange.village, participant))
        }
        return dayChange.copy(messages = messages).setIsChange(dayChange)
    }

    private fun createAutopsyMessage(village: Village, participant: VillageParticipant): Message {
        val reason = "${participant.dead!!.reason}死"
        val text = "${participant.name()}の死因は、${reason}のようだ。"
        return Message.createAutopsyPrivateMessage(text, village.day.latestDay().id)
    }
}