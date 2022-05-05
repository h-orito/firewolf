package com.ort.firewolf.api.controller

import com.ort.firewolf.api.body.PlayerUpdateDetailBody
import com.ort.firewolf.api.body.PlayerUpdateNicknameBody
import com.ort.firewolf.api.view.player.MyselfPlayerView
import com.ort.firewolf.api.view.player.PlayerRecordsView
import com.ort.firewolf.application.coordinator.PlayerCoordinator
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Villages
import com.ort.firewolf.fw.security.FirewolfUser
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
class PlayerController(
    private val playerCoordinator: PlayerCoordinator,
    private val charachipService: CharachipService,
    private val playerService: PlayerService,
    private val villageService: VillageService
) {

    // ===================================================================================
    //                                                                             Execute
    //                                                                           =========
    @GetMapping("/my-player")
    fun myPlayer(
        @AuthenticationPrincipal user: FirewolfUser
    ): MyselfPlayerView {
        val player: Player = playerService.findPlayer(user)
        val participantVillages: Villages = villageService.findVillages(
            player.participateProgressVillageIdList + player.participateFinishedVillageIdList
        )
        val createVillages: Villages = villageService.findVillages(
            player.createProgressVillageIdList + player.createFinishedVillageIdList
        )
        return MyselfPlayerView(
            player,
            participantVillages,
            createVillages,
            user
        )
    }

    @PostMapping("/player/nickname")
    fun updateNickname(
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: PlayerUpdateNicknameBody
    ) {
        playerService.updateNickname(user, body.nickname!!, body.twitterUserName!!)
    }

    @PostMapping("/player/detail")
    fun updateDetail(
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: PlayerUpdateDetailBody
    ) {
        playerService.updateDetail(user.uid, body.otherSiteName, body.introduction)
    }

    private val logger = LoggerFactory.getLogger(PlayerController::class.java)

    @GetMapping("/player/{playerId}/record")
    fun stats(
        @PathVariable("playerId") playerId: Int
    ): PlayerRecordsView {
        val player: Player = playerService.findPlayer(playerId)
        val playerRecords = playerCoordinator.findPlayerRecords(player)
        val charachipIdList =
            playerRecords.participateVillageList.flatMap { it.village.setting.charachip.charachipIds }.distinct()
        val charas: Charas = charachipService.findCharas(charachipIdList)
        val playerIdList =
            playerRecords.participateVillageList.flatMap {
                (it.village.participant.memberList + it.village.spectator.memberList).map { member -> member.playerId!! }
            }.distinct()
        val players: Players = playerService.findPlayers(playerIdList)
        val createPlayerIdList = playerRecords.participateVillageList.map { it.village.creatorPlayerId }
        val createPlayers: Players = playerService.findPlayers(createPlayerIdList)
        return PlayerRecordsView(playerRecords, charas, players, createPlayers)
    }
}