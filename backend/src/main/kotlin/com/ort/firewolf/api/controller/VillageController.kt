package com.ort.firewolf.api.controller

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.api.body.*
import com.ort.firewolf.api.body.validator.VillageRegisterBodyValidator
import com.ort.firewolf.api.form.VillageListForm
import com.ort.firewolf.api.form.VillageMessageForm
import com.ort.firewolf.api.view.charachip.CharaView
import com.ort.firewolf.api.view.message.MessageTimeView
import com.ort.firewolf.api.view.message.MessageView
import com.ort.firewolf.api.view.message.MessagesView
import com.ort.firewolf.api.view.myself.participant.SituationAsParticipantView
import com.ort.firewolf.api.view.village.*
import com.ort.firewolf.application.coordinator.MessageCoordinator
import com.ort.firewolf.application.coordinator.VillageCoordinator
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.application.service.MessageService
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.message.*
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.skill.Skills
import com.ort.firewolf.domain.model.village.*
import com.ort.firewolf.domain.model.village.participant.VillageParticipantName
import com.ort.firewolf.domain.model.village.participant.VillageParticipantNotificationCondition
import com.ort.firewolf.domain.model.village.participant.coming_out.ComingOuts
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import com.ort.firewolf.fw.security.FirewolfUser
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset


@RestController
class VillageController(
    val villageRegisterBodyValidator: VillageRegisterBodyValidator,

    val villageCoordinator: VillageCoordinator,
    val messageCoordinator: MessageCoordinator,

    val villageService: VillageService,
    val playerService: PlayerService,
    val charachipService: CharachipService,
    val messageService: MessageService
) {

    @InitBinder("villageRegisterBody")
    fun initBinder(binder: WebDataBinder) {
        binder.addValidators(villageRegisterBodyValidator)
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                           =========
    /**
     * 村一覧取得
     * @param user user
     * @param form 検索パラメータ
     */
    @GetMapping("/village/list")
    fun villageList(
        @AuthenticationPrincipal user: FirewolfUser?,
        @Validated form: VillageListForm
    ): VillagesView {
        val villageStatusList = form.village_status?.map { VillageStatus(CDef.VillageStatus.codeOf(it)) } ?: listOf()
        val villages: Villages = villageService.findVillages(
            villageStatusList = villageStatusList
        )
        return VillagesView(villages)
    }

    /**
     * 村情報取得
     * @param villageId villageId
     */
    @GetMapping("/village/{villageId}")
    fun village(@PathVariable("villageId") villageId: Int): VillageView {
        val village: Village = villageService.findVillage(villageId)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val players: Players = playerService.findPlayers(villageId)
        val createPlayer: Player = playerService.findPlayer(village.creatorPlayerId)
        return VillageView(
            village = village,
            charas = charas,
            players = players,
            createPlayer = createPlayer
        )
    }

    /**
     * アンカー発言取得
     * @param villageId villageId
     * @param messageType 発言種別
     * @param messageNumber 発言番号
     * @param user user
     */
    @GetMapping("/village/{villageId}/message/type/{messageType}/number/{messageNumber}")
    fun anchorMessage(
        @PathVariable("villageId") villageId: Int,
        @PathVariable("messageType") messageType: String,
        @PathVariable("messageNumber") messageNumber: Int,
        @AuthenticationPrincipal user: FirewolfUser?
    ): VillageAnchorMessageView {
        val village: Village = villageService.findVillage(villageId, false)
        val message: Message? = messageCoordinator.findMessage(village, messageType, messageNumber, user)
        val players: Players = playerService.findPlayers(villageId)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        return VillageAnchorMessageView(
            message = message,
            village = village,
            players = players,
            charas = charas
        )
    }

    /**
     * 発言取得
     * @param villageId villageId
     * @param day 日付
     * @param noonnight 昼夜
     * @param user user
     */
    @GetMapping("/village/{villageId}/day/{day}/time/{noonnight}/message-list")
    fun message(
        @PathVariable("villageId") villageId: Int,
        @PathVariable("day") day: Int,
        @PathVariable("noonnight") noonnight: String,
        @AuthenticationPrincipal user: FirewolfUser?,
        @Validated form: VillageMessageForm
    ): MessagesView {
        val village: Village = villageService.findVillage(villageId, false)
        val messageTypeList = form.message_type_list?.mapNotNull { CDef.MessageType.codeOf(it) }
        val messages: Messages = messageCoordinator.findMessageList(
            village = village,
            day = day,
            noonnight = noonnight,
            user = user,
            from = form.from,
            pageSize = form.page_size,
            pageNum = form.page_num,
            keyword = form.keyword,
            messageTypeList = messageTypeList,
            isLatest = form.is_disp_latest ?: false,
            fromParticipantIdList = form.participant_id_list?.filterNotNull(), // [null]で来る問題に対応
            toParticipantIdList = form.to_participant_id_list?.filterNotNull() // [null]で来る問題に対応
        )
        val players: Players = playerService.findPlayers(villageId)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val villageDayId: Int = village.day.dayList.first { it.day == day && it.noonnight == noonnight }.id
        val todayMessages =
            messageService.findMessages(village.id, villageDayId, MessageQuery(listOf(CDef.MessageType.通常発言)))
        return MessagesView(
            messages = messages,
            village = village,
            players = players,
            charas = charas,
            todayMessages = todayMessages
        )
    }

    /**
     * 最新発言時間、村日付取得
     * @param villageId villageId
     * @param user user
     */
    @GetMapping("/village/{villageId}/latest")
    fun findLatest(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser?
    ): VillageLatestView {
        val village: Village = villageService.findVillage(villageId, false)
        val unixTimeMilli = messageCoordinator.findLatestMessagesUnixTimeMilli(village, user)
        return VillageLatestView(
            unixTimeMilli = unixTimeMilli,
            villageDayId = village.day.latestDay().id
        )
    }

    /**
     * 村作成
     * @param user user
     * @param body 村設定
     */
    @PostMapping("/village")
    fun registerVillage(
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated villageRegisterBody: VillageRegisterBody
    ): VillageRegisterView {
        val player: Player = playerService.findPlayer(user)
        val village: Village = Village.createForRegister(
            resource = convertToVillageCreateResource(villageRegisterBody, player)
        )
        val villageId: Int = villageCoordinator.registerVillage(village, user)
        return VillageRegisterView(villageId = villageId)
    }

    /**
     * 村作成確認
     * @param user user
     * @param body 村設定
     */
    @PostMapping("/village/confirm")
    fun confirmRegisterVillage(
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated villageRegisterBody: VillageRegisterBody
    ) {
        val player: Player = playerService.findPlayer(user)
        val village: Village = Village.createForRegister(
            resource = convertToVillageCreateResource(villageRegisterBody, player)
        )
        villageCoordinator.confirmVillage(village, user)
    }

    /**
     * 村参加状況取得
     * @param villageId villageId
     * @param user user
     * @return 参加状況
     */
    @GetMapping("/village/{villageId}/situation")
    fun getParticipateSituation(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser?
    ): SituationAsParticipantView {
        val village: Village = villageService.findVillage(villageId)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val players: Players = playerService.findPlayers(villageId)
        return SituationAsParticipantView(
            situation = villageCoordinator.findActionSituation(village, user, players, charas),
            village = village,
            charas = charas,
            players = players
        )
    }

    /**
     * 村参加確認
     * @param villageId villageId
     * @param user user
     * @param body 村参加に必要な情報
     */
    @PostMapping("/village/{villageId}/participate-confirm")
    fun participateConfirm(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageParticipateBody
    ): MessageView {
        val isSpectate = body.spectator ?: false
        villageCoordinator.assertParticipate(
            villageId = villageId,
            user = user,
            charaId = body.charaId!!,
            message = body.joinMessage!!,
            isSpectate = isSpectate,
            firstRequestSkill = CDef.Skill.codeOf(body.firstRequestSkill),
            secondRequestSkill = CDef.Skill.codeOf(body.secondRequestSkill),
            password = body.joinPassword
        )
        val messageContent = MessageContent.invoke(
            if (isSpectate) CDef.MessageType.見学発言.code() else CDef.MessageType.通常発言.code(),
            body.joinMessage,
            CDef.FaceType.通常.code()
        ).copy(num = 0)
        val chara: Chara = charachipService.findChara(body.charaId)
        return MessageView(
            from = VillageParticipantView(
                id = 1, // dummy
                name = VillageParticipantName(
                    name = chara.charaName.name,
                    shortName = chara.charaName.shortName
                ).fullName(),
                charaName = VillageParticipantName(
                    name = chara.charaName.name,
                    shortName = chara.charaName.shortName
                ),
                chara = CharaView(chara),
                player = null,
                dead = null,
                spectator = body.spectator ?: false,
                skill = null,
                skillRequest = null,
                win = null,
                commingOuts = ComingOuts(),
                notification = null
            ),
            to = null,
            time = MessageTimeView(
                villageDayId = 1, // dummy
                day = 0,
                datetime = LocalDateTime.now(),
                unixTimeMilli = LocalDateTime.now().toInstant(ZoneOffset.ofHours(+9)).toEpochMilli()
            ),
            content = messageContent
        )
    }

    /**
     * 村に参加
     * @param villageId villageId
     * @param user user
     * @param body 村設定
     */
    @PostMapping("/village/{villageId}/participate")
    fun participateVillage(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageParticipateBody
    ) {
        villageCoordinator.assertParticipate(
            villageId = villageId,
            user = user,
            charaId = body.charaId!!,
            message = body.joinMessage!!,
            isSpectate = body.spectator ?: false,
            firstRequestSkill = CDef.Skill.codeOf(body.firstRequestSkill),
            secondRequestSkill = CDef.Skill.codeOf(body.secondRequestSkill),
            password = body.joinPassword
        )
        val player = playerService.findPlayer(user)
        villageCoordinator.participate(
            villageId = villageId,
            playerId = player.id,
            charaId = body.charaId,
            message = body.joinMessage,
            isSpectate = body.spectator ?: false,
            firstRequestSkill = CDef.Skill.codeOf(body.firstRequestSkill),
            secondRequestSkill = CDef.Skill.codeOf(body.secondRequestSkill),
            ipAddress = user.ipAddress!!
        )
    }

    /**
     * 希望役職変更
     * @param villageId villageId
     * @param user user
     * @param body 役職
     */
    @PostMapping("/village/{villageId}/change-skill")
    fun changeSkill(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageChangeSkillBody
    ) {
        villageCoordinator.changeSkillRequest(villageId, user, body.firstRequestSkill!!, body.secondRequestSkill!!)
    }

    /**
     * 名前変更
     * @param villageId villageId
     * @param user user
     * @param body 役職
     */
    @PostMapping("/village/{villageId}/change-name")
    fun changeName(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageChangeNameBody
    ) {
        villageCoordinator.changeName(villageId, user, body.name!!, body.shortName!!)
    }

    /**
     * 退村
     * @param villageId villageId
     * @param user user
     */
    @PostMapping("/village/{villageId}/leave")
    fun leave(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser
    ) {
        villageCoordinator.leave(villageId, user)
    }

    /**
     * 発言確認
     * @param villageId villageId
     * @param user user
     * @param body 発言内容
     */
    @PostMapping("/village/{villageId}/say-confirm")
    @ResponseBody
    fun sayConfirm(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageSayBody
    ): MessageView {
        villageCoordinator.confirmToSay(
            villageId,
            user,
            body.message!!,
            body.messageType!!,
            body.faceType!!,
            body.targetId
        )
        val village = villageService.findVillage(villageId)
        val participant = villageCoordinator.findParticipant(village, user)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val players: Players = playerService.findPlayers(villageId)
        val target = body.targetId?.let { village.allParticipants().member(it) }
        return MessageView(
            message = Message(
                fromVillageParticipantId = participant!!.id,
                fromCharacterName = participant.name(),
                toVillageParticipantId = body.targetId,
                toCharacterName = target?.name(),
                time = MessageTime(
                    villageDayId = village.day.latestDay().id,
                    datetime = LocalDateTime.now(),
                    unixTimeMilli = LocalDateTime.now().toInstant(ZoneOffset.ofHours(+9)).toEpochMilli()
                ),
                content = MessageContent.invoke(
                    messageType = body.messageType,
                    text = body.message,
                    faceCode = body.faceType
                ).copy(num = 1)
            ),
            village = village,
            players = players,
            charas = charas,
            shouldHidePlayer = true
        )
    }

    /**
     * 発言
     * @param villageId villageId
     * @param user user
     * @param body 発言内容
     */
    @PostMapping("/village/{villageId}/say")
    fun say(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageSayBody
    ) {
        villageCoordinator.say(villageId, user, body.message!!, body.messageType!!, body.faceType!!, body.targetId)
    }

    /**
     * アクション確認
     * @param villageId villageId
     * @param user user
     * @param body 発言内容
     */
    @PostMapping("/village/{villageId}/action-confirm")
    @ResponseBody
    fun actionConfirm(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageActionBody
    ): MessageView {
        villageCoordinator.confirmToSay(villageId, user, body.message!!, CDef.MessageType.アクション.code(), null)
        val village = villageService.findVillage(villageId)
        val participant = villageCoordinator.findParticipant(village, user)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val players: Players = playerService.findPlayers(villageId)
        return MessageView(
            message = Message(
                fromVillageParticipantId = participant!!.id,
                fromCharacterName = participant.name(),
                toVillageParticipantId = null,
                toCharacterName = null,
                time = MessageTime(
                    villageDayId = village.day.latestDay().id,
                    datetime = LocalDateTime.now(),
                    unixTimeMilli = LocalDateTime.now().toInstant(ZoneOffset.ofHours(+9)).toEpochMilli()
                ),
                content = MessageContent.invoke(
                    messageType = CDef.MessageType.アクション.toModel().code,
                    text = "${body.myself!!}${body.target ?: ""}${body.message!!}",
                    faceCode = null
                ).copy(num = 1)
            ),
            village = village,
            players = players,
            charas = charas,
            shouldHidePlayer = true
        )
    }

    /**
     * アクション
     * @param villageId villageId
     * @param user user
     * @param body 発言内容
     */
    @PostMapping("/village/{villageId}/action")
    fun say(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageActionBody
    ) {
        villageCoordinator.action(villageId, user, body.myself!!, body.target, body.message!!)
    }

    /**
     * 能力セット
     *
     * @param villageId villageId
     * @param user user
     * @param body 能力セット内容
     */
    @PostMapping("/village/{villageId}/ability")
    fun ability(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageAbilityBody
    ) {
        villageCoordinator.setAbility(villageId, user, body.myselfId, body.targetId, body.abilityType!!)
    }

    /**
     * 投票セット
     * @param villageId villageId
     * @param user user
     * @param body 投票内容
     */
    @PostMapping("/village/{villageId}/vote")
    fun vote(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageVoteBody
    ) {
        villageCoordinator.setVote(villageId, user, body.targetId!!)
    }

    /**
     * コミットセット
     * @param villageId villageId
     * @param user user
     * @param body コミット/取り消し
     */
    @PostMapping("/village/{villageId}/commit")
    fun commit(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageCommitBody
    ) {
        villageCoordinator.setCommit(villageId, user, body.commit!!)
    }

    /**
     * カミングアウトセット
     * @param villageId villageId
     * @param user user
     * @param body co内容
     */
    @PostMapping("/village/{villageId}/comingout")
    fun comingout(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageComingOutBody
    ) {
        val skills = if (body.skillCode.isNullOrEmpty()) Skills(listOf())
        else Skills(body.skillCode.map { Skill(CDef.Skill.codeOf(it)) })
        villageCoordinator.setComingOut(
            villageId,
            user,
            skills
        )
    }

    // 設定保存
    @PostMapping("/village/{villageId}/notification-setting")
    fun saveNotification(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated body: VillageNotificationBody,  //
    ) {
        villageCoordinator.saveNotification(
            villageId,
            user,
            VillageParticipantNotificationCondition(
                discordWebhookUrl = body.webhookUrl!!,
                village = VillageParticipantNotificationCondition.VillageCondition(
                    start = body.villageStart ?: false,
                    dayChange = body.villageDaychange ?: false,
                    epilogue = body.villageEpilogue ?: false
                ),
                message = VillageParticipantNotificationCondition.MessageCondition(
                    secretSay = body.secretSay ?: false,
                    abilitySay = body.abilitySay ?: false,
                    anchor = body.anchorSay ?: false,
                    keywords = body.keyword?.trim()?.replace("　", " ")?.split(" ") ?: emptyList()
                )
            )
        )
    }

    /**
     * 村設定変更確認
     * @param villageId villageId
     * @param user user
     * @param body 村設定内容
     */
    @PostMapping("/village/{villageId}/setting/confirm")
    fun settingConfirm(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated villageRegisterBody: VillageRegisterBody
    ) {
        val village = villageService.findVillage(villageId)
        val player = playerService.findPlayer(user)

        if (user.authority != CDef.Authority.管理者 && village.creatorPlayerId != player.id)
            throw FirewolfBusinessException("村建てか管理者しか使えません")

        val createResource = convertToVillageCreateResource(villageRegisterBody, player).copy(
            createPlayerId = village.creatorPlayerId // 管理者に上書きされるのを防ぐ
        )
        villageCoordinator.assertModifySetting(village, player, createResource)
    }

    /**
     * 村設定変更
     * @param villageId villageId
     * @param user user
     * @param body 村設定内容
     */
    @PostMapping("/village/{villageId}/setting")
    fun setting(
        @PathVariable("villageId") villageId: Int,
        @AuthenticationPrincipal user: FirewolfUser,
        @RequestBody @Validated villageRegisterBody: VillageRegisterBody
    ) {
        val village = villageService.findVillage(villageId)
        val player = playerService.findPlayer(user)

        if (user.authority != CDef.Authority.管理者 && village.creatorPlayerId != player.id)
            throw FirewolfBusinessException("村建てか管理者しか使えません")

        val createResource = convertToVillageCreateResource(villageRegisterBody, player)
        villageCoordinator.modifySetting(village, player, createResource)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun convertToVillageCreateResource(
        body: VillageRegisterBody,
        player: Player
    ): VillageCreateResource {
        return VillageCreateResource(
            villageName = body.villageName!!,
            createPlayerId = player.id,
            setting = convertToVillageSettingCreateResource(body.setting!!)
        )
    }

    private fun convertToVillageSettingCreateResource(
        body: VillageSettingRegisterBody
    ): VillageSettingCreateResource {
        return VillageSettingCreateResource(
            time = convertToVillageTimeCreateResource(body.time!!),
            organization = convertToVillageOrganizationCreateResource(body.organization!!),
            charachip = convertToVillageCharachipCreateResource(body.charachip!!),
            rule = convertToVillageRuleCreateResource(body.rule!!)

        )
    }

    private fun convertToVillageTimeCreateResource(
        body: VillageTimeCreateBody
    ): VillageTimeCreateResource = VillageTimeCreateResource(
        startDatetime = body.startDatetime!!,
        silentHours = body.silentHours
    )

    private fun convertToVillageOrganizationCreateResource(
        body: VillageOrganizationCreateBody
    ): VillageOrganizationCreateResource = VillageOrganizationCreateResource(
        organization = body.organization!!
    )

    private fun convertToVillageCharachipCreateResource(
        body: VillageCharachipCreateBody
    ): VillageCharachipCreateResource = VillageCharachipCreateResource(
        charachipIds = body.charachipIds!!,
        dummyCharaId = body.dummyCharaId!!
    )

    private fun convertToVillageRuleCreateResource(
        body: VillageRuleCreateBody
    ): VillageRuleCreateResource = VillageRuleCreateResource(
        isOpenVote = body.openVote!!,
        isAvailableSkillRequest = body.availableSkillRequest!!,
        isAvailableSpectate = body.availableSpectate!!,
        isOpenSkillInGrave = body.openSkillInGrave!!,
        isVisibleGraveMessage = body.visibleGraveMessage!!,
        isAvailableSuddenlyDeath = body.availableSuddenlyDeath!!,
        isAvailableCommit = body.availableCommit!!,
        isAvailableDummySkill = body.availableDummySkill!!,
        isAvailableAction = body.availableAction!!,
        isAvailableSecretSay = body.availableSecretSay!!,
        restrictList = body.restrictList!!.map {
            VillageMessageRestrictCreateResource(
                type = MessageType(CDef.MessageType.codeOf(it.type!!)),
                count = it.count!!,
                length = it.length!!
            )
        },
        joinPassword = body.joinPassword
    )
}