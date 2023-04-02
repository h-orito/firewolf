package com.ort.firewolf.application.service

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.fw.security.FirewolfUser
import com.ort.firewolf.infrastructure.datasource.player.PlayerDataSource
import org.springframework.stereotype.Service

@Service
class PlayerService(
    private val playerDataSource: PlayerDataSource,
    private val tweetService: TweetService
) {

    fun findPlayer(id: Int): Player = playerDataSource.findPlayer(id)

    fun findPlayer(user: FirewolfUser): Player = playerDataSource.findPlayer(user.uid)

    fun findPlayers(villageId: Int): Players = playerDataSource.findPlayers(villageId)

    fun findPlayers(playerIdList: List<Int>): Players = playerDataSource.findPlayers(playerIdList)

    fun updateNickname(user: FirewolfUser, nickname: String, twitterUserName: String?) {
        val twitterUserId = twitterUserName?.let { tweetService.getUserIdByUsername(twitterUserName) }
        playerDataSource.update(user.uid, nickname, twitterUserName, twitterUserId)
    }

    fun updateDetail(
        uid: String,
        nickname: String,
        otherSiteName: String?,
        introduction: String?
    ) {
        playerDataSource.updateDetail(uid, nickname, otherSiteName, introduction)
    }

    fun updateDifference(before: Players, after: Players) = playerDataSource.updateDifference(before, after)
}