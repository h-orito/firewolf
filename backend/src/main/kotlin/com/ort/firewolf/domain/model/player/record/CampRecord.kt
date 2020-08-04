package com.ort.firewolf.domain.model.player.record

import com.ort.firewolf.domain.model.camp.Camp
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Villages

data class CampRecord(
    val camp: Camp,
    val participateCount: Int,
    val winCount: Int,
    val winRate: Float
) {
    constructor(
        camp: Camp,
        player: Player,
        villages: Villages
    ) : this(
        camp = camp,
        participateCount = participantCount(villages, player),
        winCount = sumWinCount(villages, player),
        winRate = if (participantCount(villages, player) == 0) 0F
        else sumWinCount(villages, player).toFloat() / participantCount(villages, player).toFloat()
    )

    companion object {
        private fun participantCount(villages: Villages, player: Player): Int {
            return villages.list.count { village ->
                val isSpectator = village.findMemberByPlayerId(player.id)?.isSpectator ?: true
                !isSpectator
            }
        }

        private fun sumWinCount(villages: Villages, player: Player): Int {
            return villages.list.count { village ->
                village.findMemberByPlayerId(player.id)?.isWin ?: false
            }
        }
    }
}
