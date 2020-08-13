package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class AutopsyDomainService {

    fun addAutopsyMessage(dayChange: DayChange, charas: Charas): DayChange {
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
            messages = messages.add(createAutopsyMessage(dayChange.village, charas, participant))
        }
        return dayChange.copy(messages = messages).setIsChange(dayChange)
    }

    private fun createAutopsyMessage(village: Village, charas: Charas, participant: VillageParticipant): Message {
        val chara = charas.chara(participant.charaId)
        val reason = "${participant.dead!!.reason}死"
        val text = "${chara.charaName.fullName()}の死因は、${reason}のようだ。"
        return Message.createAutopsyPrivateMessage(text, village.day.latestDay().id)
    }
}