package com.ort.firewolf.domain.service.vote

import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.myself.participant.VillageVoteSituation
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.vote.VillageVote
import com.ort.firewolf.domain.model.village.vote.VillageVotes
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class VoteDomainService {

    fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        votes: VillageVotes
    ): VillageVoteSituation {
        return VillageVoteSituation(
            isAvailableVote = isAvailableVote(village, participant),
            targetList = getSelectableTargetList(village, participant),
            target = getSelectingTarget(village, participant, votes)
        )
    }

    fun assertVote(village: Village, participant: VillageParticipant?, targetId: Int) {
        if (!isAvailableVote(village, participant)) throw FirewolfBusinessException("投票できません")
        if (getSelectableTargetList(
                village,
                participant
            ).none { it.id == targetId }
        ) throw FirewolfBusinessException("投票できません")
    }

    /**
     * 投票結果メッセージ
     */
    fun createEachVoteMessage(
        village: Village,
        votedMap: Map<Int, List<VillageVote>>
    ): Message {
        val maxFromCharaNameLength = votedMap.values.flatten().map { vote ->
            village.participant.member(vote.myselfId).name().length
        }.max()!!
        val maxToCharaNameLength = votedMap.values.flatten().map { vote ->
            village.participant.member(vote.targetId).name().length
        }.max()!!

        val text = votedMap.entries.sortedBy { it.value.size }.reversed().map { entry ->
            // 得票数が多い順
            entry.value.map { vote ->
                createEachVoteResultString(
                    village.participant.member(vote.myselfId),
                    village.participant.member(vote.targetId),
                    maxFromCharaNameLength,
                    maxToCharaNameLength,
                    entry.value.size
                )
            }
        }.flatten().joinToString(
            prefix = "投票結果は以下の通り。\n",
            separator = "\n"
        )

        return if (village.setting.rules.openVote) {
            Message.createPublicSystemMessage(text, village.day.latestDay().id)
        } else {
            Message.createPrivateSystemMessage(text, village.day.latestDay().id)
        }
    }

    fun addDefaultVote(dayChange: DayChange): DayChange {
        val village = dayChange.village
        var votes = dayChange.votes

        // 最新日
        val latestVillageDayId = village.day.latestDay().id
        // 生存している人だけ自分に投票
        val newVoteList = village.participant.filterAlive().memberList.map { member ->
            VillageVote(
                villageDayId = latestVillageDayId,
                myselfId = member.id,
                targetId = member.id
            )
        }
        return dayChange.copy(
            votes = votes.addAll(newVoteList)
        )
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * @return {キャラ名} -> {キャラ名}（{N}票）
     */
    private fun createEachVoteResultString(
        from: VillageParticipant,
        target: VillageParticipant,
        maxFromCharaNameLength: Int,
        maxToCharaNameLength: Int,
        count: Int
    ): String {
        val fromString = from.name().padEnd(
            length = maxFromCharaNameLength,
            padChar = '　'
        )
        val toString = target.name().padEnd(
            length = maxToCharaNameLength,
            padChar = '　'
        )
        return "$fromString → ${toString}(${count}票)"
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isAvailableVote(village: Village, participant: VillageParticipant?): Boolean {
        // 参加者として可能か
        participant ?: return false
        if (!participant.isAvailableVote()) return false
        // 村として可能か
        return village.isAvailableVote()
    }

    fun getSelectableTargetList(village: Village, participant: VillageParticipant?): List<VillageParticipant> {
        if (!isAvailableVote(village, participant)) return listOf()
        return village.participant.memberList.filter { it.isAlive() }
    }

    fun getSelectingTarget(
        village: Village,
        participant: VillageParticipant?,
        votes: VillageVotes
    ): VillageParticipant? {
        if (!isAvailableVote(village, participant)) return null
        val voteTargetParticipantId = votes.list.find {
            it.villageDayId == village.day.latestDay().id
                    && it.myselfId == participant!!.id
        }?.targetId ?: return null
        return village.participant.member(voteTargetParticipantId)
    }
}