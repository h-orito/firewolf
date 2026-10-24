package com.ort.firewolf.api.controller

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.api.body.CreatorKickBody
import com.ort.firewolf.api.body.CreatorSayBody
import com.ort.firewolf.api.view.message.MessageView
import com.ort.firewolf.application.coordinator.VillageCoordinator
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.message.MessageTime
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import com.ort.firewolf.fw.security.FirewolfUser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestController
class CreatorController(
    private val villageService: VillageService,
    private val playerService: PlayerService,
    private val villageCoordinator: VillageCoordinator,
) {
    @PostMapping("/creator/village/{villageId}/kick")
    fun kick(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: CreatorKickBody,
    ) {
        villageCoordinator.kick(villageId, user, body.targetId!!)
    }

    @PostMapping("/creator/village/{villageId}/cancel")
    fun cancel(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
    ) {
        villageCoordinator.cancelVillage(villageId, user)
    }

    @PostMapping("/creator/village/{villageId}/say-confirm")
    fun sayConfirm(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: CreatorSayBody,
    ): MessageView {
        val village = villageService.findVillage(villageId)
        val player = playerService.findPlayer(user)

        if (user.authority != CDef.Authority.管理者 && village.creatorPlayerId != player.id) {
            throw FirewolfBusinessException("村建てか管理者しか使えません")
        }

        villageCoordinator.confirmToCreatorSay(village, body.message!!)
        return MessageView(
            message =
                Message(
                    fromVillageParticipantId = null,
                    fromCharacterName = null,
                    toVillageParticipantId = null,
                    toCharacterName = null,
                    time =
                        MessageTime(
                            villageDayId = village.day.latestDay().id,
                            datetime = LocalDateTime.now(),
                            unixTimeMilli = LocalDateTime.now().toInstant(ZoneOffset.ofHours(+9)).toEpochMilli(),
                        ),
                    content =
                        MessageContent.invoke(
                            messageType = CDef.MessageType.村建て発言.code(),
                            text = body.message,
                            faceCode = null,
                        ).copy(num = 1),
                ),
            village = village,
            players = Players(listOf()),
            charas = Charas(listOf()),
            shouldHidePlayer = true,
        )
    }

    @PostMapping("/creator/village/{villageId}/say")
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    @CacheEvict(cacheNames = ["messages", "latest-messages"], allEntries = true)
    fun say(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: CreatorSayBody,
    ) {
        val village = villageService.findVillage(villageId)
        val player = playerService.findPlayer(user)

        if (user.authority != CDef.Authority.管理者 && village.creatorPlayerId != player.id) {
            throw FirewolfBusinessException("村建てか管理者しか使えません")
        }

        villageCoordinator.confirmToCreatorSay(village, body.message!!)
        villageCoordinator.creatorSay(village, body.message)
    }

    @PostMapping("/creator/village/{villageId}/extend-epilogue")
    fun extendEpilogue(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
    ) {
        villageCoordinator.extendEpilogue(villageId, user)
    }
}
