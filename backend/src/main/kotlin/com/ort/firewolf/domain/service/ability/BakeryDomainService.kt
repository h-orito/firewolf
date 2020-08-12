package com.ort.firewolf.domain.service.ability

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.VillageDay
import org.springframework.stereotype.Service

@Service
class BakeryDomainService {

    fun addBakeryMessage(dayChange: DayChange): DayChange {
        val bakeryParticipantList = dayChange.village.participant.memberList.filter {
            it.skill!!.toCdef().isHasBakeryAbility
        }
        // 1人でも生存していたらパンを焼く
        val latestDay = dayChange.village.day.latestDay()
        if (bakeryParticipantList.any { it.isAlive() }) {
            return dayChange.copy(
                messages = dayChange.messages.add(createBakeMessage(latestDay))
            ).setIsChange(dayChange)
        }
        // 今日全員死亡した場合も専用メッセージ
        if (bakeryParticipantList.none { it.isAlive() }
            && bakeryParticipantList.any { it.dead?.villageDay?.id == latestDay.id }) {
            return dayChange.copy(
                messages = dayChange.messages.add(createBakeryGoneMessage(latestDay))
            ).setIsChange(dayChange)
        }
        return dayChange
    }

    private fun createBakeMessage(latestDay: VillageDay): Message {
        return Message.createPublicSystemMessage(
            "パン屋がおいしいパンを焼いてくれたそうです。",
            latestDay.id
        )
    }

    private fun createBakeryGoneMessage(latestDay: VillageDay): Message {
        return Message.createPublicSystemMessage(
            "今日からはもうおいしいパンが食べられません。",
            latestDay.id
        )
    }
}