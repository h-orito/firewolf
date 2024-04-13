package com.ort.firewolf.domain.service.daychange

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class MiserableDeathDomainService {

    fun processDayChangeAction(dayChange: DayChange): DayChange {
        val latestDay = dayChange.village.day.latestDay()

        val miserableDeathParticipantList = dayChange.village.participant.memberList.filter {
            !it.isAlive() && it.dead?.villageDay?.id == latestDay.id && it.dead.toCdef().isMiserableDeath
        }

        return dayChange.copy(
            messages = dayChange.messages.add(
                createMiserableDeathMessage(
                    dayChange.village,
                    miserableDeathParticipantList
                )
            )
        ).setIsChange(dayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * 無惨メッセージ
     */
    private fun createMiserableDeathMessage(
        village: Village,
        miserableDeathParticipantList: List<VillageParticipant>
    ): Message {
        val text = if (miserableDeathParticipantList.isEmpty()) {
            "今日は犠牲者がいないようだ。人狼は襲撃に失敗したのだろうか。"
        } else {
            miserableDeathParticipantList.shuffled().joinToString(
                prefix = "次の日の朝、以下の村人が無惨な姿で発見された。\n",
                separator = "、\n"
            ) { it.name() }
        }
        return Message.createPublicSystemMessage(text, village.day.latestDay().id)
    }
}