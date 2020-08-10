package com.ort.firewolf.domain.model.village.vote

import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageDay

data class VillageVotes(
    val list: List<VillageVote>
) {

    fun filterLatestday(village: Village): VillageVotes = filterByDay(village.day.latestDay())

    fun filterYesterday(village: Village): VillageVotes = filterByDay(village.day.yesterday())

    fun addAll(voteList: List<VillageVote>): VillageVotes {
        if (voteList.isEmpty()) return this
        return this.copy(
            list = list + voteList
        )
    }

    fun existsDifference(votes: VillageVotes): Boolean {
        return list.size != votes.list.size
    }

    private fun filterByDay(villageDay: VillageDay): VillageVotes {
        return this.copy(
            list = list.filter { it.villageDayId == villageDay.id }
        )
    }
}