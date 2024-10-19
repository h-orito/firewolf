package com.ort.firewolf.application.coordinator

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.application.service.AbilityService
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.application.service.ComingOutService
import com.ort.firewolf.application.service.CommitService
import com.ort.firewolf.application.service.MessageService
import com.ort.firewolf.application.service.NotificationService
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.application.service.VoteService
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.commit.Commit
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.myself.participant.SituationAsParticipant
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.skill.SkillRequest
import com.ort.firewolf.domain.model.skill.Skills
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageCreateResource
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.participant.VillageParticipantNotificationCondition
import com.ort.firewolf.domain.model.village.vote.VillageVote
import com.ort.firewolf.domain.model.village.vote.VillageVotes
import com.ort.firewolf.domain.service.ability.AbilityDomainService
import com.ort.firewolf.domain.service.admin.AdminDomainService
import com.ort.firewolf.domain.service.coming_out.ComingOutDomainService
import com.ort.firewolf.domain.service.commit.CommitDomainService
import com.ort.firewolf.domain.service.creator.CreatorDomainService
import com.ort.firewolf.domain.service.message.say.SayDomainService
import com.ort.firewolf.domain.service.participate.ParticipateDomainService
import com.ort.firewolf.domain.service.rp.RpDomainService
import com.ort.firewolf.domain.service.skill.SkillRequestDomainService
import com.ort.firewolf.domain.service.village.VillageSettingDomainService
import com.ort.firewolf.domain.service.vote.VoteDomainService
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import com.ort.firewolf.fw.security.FirewolfUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class VillageCoordinator(
    // application service
    private val dayChangeCoordinator: DayChangeCoordinator,
    private val accessInfoCoordinator: AccessInfoCoordinator,
    private val villageService: VillageService,
    private val playerService: PlayerService,
    private val messageService: MessageService,
    private val charachipService: CharachipService,
    private val abilityService: AbilityService,
    private val voteService: VoteService,
    private val commitService: CommitService,
    private val comingOutService: ComingOutService,
    private val notificationService: NotificationService,
    // domain service
    private val participateDomainService: ParticipateDomainService,
    private val skillRequestDomainService: SkillRequestDomainService,
    private val commitDomainService: CommitDomainService,
    private val sayDomainService: SayDomainService,
    private val abilityDomainService: AbilityDomainService,
    private val voteDomainService: VoteDomainService,
    private val creatorDomainService: CreatorDomainService,
    private val villageSettingDomainService: VillageSettingDomainService,
    private val comingOutDomainService: ComingOutDomainService,
    private val adminDomainService: AdminDomainService,
    private val rpDomainService: RpDomainService
) {

    /**
     * 村参加者取得
     * @param village village
     * @param user user
     * @return 村参加者
     */
    fun findParticipant(village: Village, user: FirewolfUser?): VillageParticipant? {
        user ?: return null
        val player: Player = playerService.findPlayer(user)
        return village.findMemberByPlayerId(player.id)
    }

    /**
     * 村登録確認
     * @param paramVillage village
     * @param user user
     */
    fun confirmVillage(paramVillage: Village, user: FirewolfUser) {
        // 作成できない状況ならエラー
        val player: Player = playerService.findPlayer(user)
        player.assertCreateVillage(user)
    }

    /**
     * 村登録
     * @param paramVillage village
     * @param user user
     * @return 村ID
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun registerVillage(paramVillage: Village, user: FirewolfUser): Int {
        // 作成できない状況ならエラー
        val player: Player = playerService.findPlayer(user)
        player.assertCreateVillage(user)
        // 村を登録
        val village: Village = registerVillage(paramVillage)

        return village.id
    }

    /**
     * 村設定変更確認
     *
     * @param village village
     * @param player player
     * @param resource resource
     */
    fun assertModifySetting(village: Village, player: Player, resource: VillageCreateResource) {
        if (!creatorDomainService.convertToSituation(village, player).availableModifySetting) {
            throw FirewolfBusinessException("設定を変更できません")
        }
        villageSettingDomainService.assertModify(village, resource)
    }

    /**
     * 村設定変更
     *
     * @param village village
     * @param player player
     * @param resource resource
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun modifySetting(village: Village, player: Player, resource: VillageCreateResource) {
        assertModifySetting(village, player, resource)
        // 変更なしの場合もある
        villageSettingDomainService.createModifyMessage(village, resource)?.let { message ->
            messageService.registerMessage(village, message)
            var changedVillage = Village.createForUpdate(village, resource)
            if (!resource.setting.rule.isAvailableSkillRequest) {
                changedVillage = changedVillage.changeAllSkillRequestLeftover()
            }
            villageService.updateVillageDifference(village, changedVillage)
        }
    }

    /**
     * 村に参加できるかチェック
     * @param villageId villageId
     * @param user user
     * @param charaId charaId
     * @param message 入村発言
     * @param isSpectate 見学か
     * @param firstRequestSkill 役職第1希望
     * @param secondRequestSkill 役職第2希望
     * @param password 入村パスワード
     */
    fun assertParticipate(
        villageId: Int,
        user: FirewolfUser,
        charaId: Int,
        message: String,
        isSpectate: Boolean,
        firstRequestSkill: CDef.Skill = CDef.Skill.おまかせ,
        secondRequestSkill: CDef.Skill = CDef.Skill.おまかせ,
        password: String?
    ) {
        // 参加できない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val player: Player = playerService.findPlayer(user)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)

        if (isSpectate) {
            participateDomainService.assertSpectate(
                player,
                village,
                charaId,
                charas.list.size,
                password
            )
        } else {
            participateDomainService.assertParticipate(
                player,
                village,
                charaId,
                firstRequestSkill,
                secondRequestSkill,
                password
            )
        }
        // 参加発言
        val messageContent = MessageContent.invoke(
            CDef.MessageType.通常発言.code(),
            message,
            CDef.FaceType.通常.code()
        )
        val chara = charas.chara(charaId)
        sayDomainService.assertParticipateSay(village, chara, messageContent)
    }

    /**
     * 村に参加
     * @param villageId villageId
     * @param playerId playerId
     * @param charaId charaId
     * @param message 入村時発言
     * @param isSpectate 見学か
     * @param firstRequestSkill 役職第1希望
     * @param secondRequestSkill 役職第2希望
     * @param ipAddress ipAddress
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun participate(
        villageId: Int,
        playerId: Int,
        charaId: Int,
        charaShortName: String,
        charaName: String,
        message: String,
        isSpectate: Boolean,
        firstRequestSkill: CDef.Skill = CDef.Skill.おまかせ,
        secondRequestSkill: CDef.Skill = CDef.Skill.おまかせ,
        ipAddress: String
    ) {
        // 村参加者登録
        var village: Village = villageService.findVillage(villageId)
        val chara: Chara = charachipService.findChara(charaId)
        val changedVillage: Village = village.participate(
            playerId = playerId,
            chara = chara,
            charaShortName = charaShortName,
            charaName = charaName,
            firstRequestSkill = firstRequestSkill,
            secondRequestSkill = secondRequestSkill,
            isSpectate = isSpectate,
            ipAddress = ipAddress
        )
        village = villageService.updateVillageDifference(village, changedVillage)
        val myself: VillageParticipant = village.memberByPlayerId(playerId)

        // {N}人目、{キャラ名} とユーザー入力の発言
        messageService.registerParticipateMessage(
            village = village,
            participant = village.findMemberById(myself.id)!!,
            chara = chara,
            charaName = charaName,
            charaShortName = charaShortName,
            message = message,
            isSpectate = isSpectate
        )
        // IPアドレスが重複している人がいたら通知
        accessInfoCoordinator.registerAccessInfo(village, myself, ipAddress)
    }

    /**
     * 役職希望変更
     * @param villageId villageId
     * @param user user
     * @param firstRequestSkill 第1希望
     * @param secondRequestSkill 第2希望
     */
    fun changeSkillRequest(villageId: Int, user: FirewolfUser, firstRequestSkill: String, secondRequestSkill: String) {
        // 役職希望変更できない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        skillRequestDomainService.assertSkillRequest(village, participant, firstRequestSkill, secondRequestSkill)
        // 役職希望変更
        val changedVillage: Village = village.changeSkillRequest(
            participant!!.id,
            CDef.Skill.codeOf(firstRequestSkill)!!,
            CDef.Skill.codeOf(secondRequestSkill)!!
        )
        villageService.updateVillageDifference(village, changedVillage)
    }

    /**
     * 名前変更
     */
    fun changeName(villageId: Int, user: FirewolfUser, name: String, shortName: String) {
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        // 名前変更
        val changedVillage: Village = village.changeName(
            participant!!.id,
            name,
            shortName
        )
        villageService.updateVillageDifference(village, changedVillage)
        // システムメッセージ
        messageService.registerChangeNameMessage(
            village = village,
            before = participant,
            after = changedVillage.memberById(participant.id),
        )
    }

    /**
     * 退村
     * @param villageId villageId
     * @param user user
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun leave(villageId: Int, user: FirewolfUser) {
        // 退村できない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        participateDomainService.assertLeave(village, participant)
        // 退村
        val updatedVillage: Village = villageService.updateVillageDifference(
            village,
            village.leaveParticipant(participant!!.id)
        )
        // 退村メッセージ
        messageService.registerLeaveMessage(updatedVillage, participant)
    }

    /**
     * 発言できるか確認
     *
     * @param villageId villageId
     * @param user user
     * @param messageText 発言内容
     * @param messageType 発言種別
     * @param faceType 表情種別
     * @param targetId 秘話相手
     */
    fun confirmToSay(
        villageId: Int,
        user: FirewolfUser,
        messageText: String,
        messageType: String,
        faceType: String?,
        targetId: Int? = null
    ) {
        val messageContent: MessageContent = MessageContent.invoke(messageType, messageText, faceType)
        // 発言できない状況ならエラー
        assertSay(villageId, user, messageContent, targetId)
    }

    fun confirmToCreatorSay(village: Village, messageText: String) {
        val messageContent: MessageContent = MessageContent.invoke(CDef.MessageType.村建て発言.code(), messageText, null)
        // 発言できない状況ならエラー
        sayDomainService.assertCreatorSay(village, messageContent)
    }

    /**
     * 発言
     *
     * @param villageId villageId
     * @param user user
     * @param messageText 発言内容
     * @param messageType 発言種別
     * @param faceType 表情種別
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun say(
        villageId: Int,
        user: FirewolfUser,
        messageText: String,
        messageType: String,
        faceType: String?,
        targetId: Int?
    ) {
        val messageContent: MessageContent = MessageContent.invoke(messageType, messageText, faceType)
        // 発言できない状況ならエラー
        assertSay(villageId, user, messageContent, targetId)
        // 発言
        val village: Village = villageService.findVillage(villageId)
        val myself: VillageParticipant = findParticipant(village, user)!!
        val toParticipant: VillageParticipant? = targetId?.let { village.allParticipants().member(targetId) }
        val message: Message =
            Message.createSayMessage(myself, village.day.latestDay().id, messageContent, toParticipant)
        val registered = messageService.registerMessage(village, message)
        // 通知
        val players = playerService.findPlayers(village.id)
        val charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        notificationService.notifyReceiveMessageToCustomerIfNeeded(village, players, registered)
        // IPアドレス更新
        accessInfoCoordinator.registerAccessInfo(village, myself, user.ipAddress!!)
    }

    /**
     * アクション
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun action(
        villageId: Int,
        user: FirewolfUser,
        myselfText: String,
        targetText: String?,
        messageText: String
    ) {
        // 発言できない状況ならエラー
        assertSay(villageId, user, MessageContent.invoke(CDef.MessageType.アクション.code(), messageText, null), null)
        // 発言
        val village: Village = villageService.findVillage(villageId)
        val myself: VillageParticipant = findParticipant(village, user)!!
        val text = "${myselfText}${targetText ?: ""}${messageText}"
        val message: Message = Message.createSayMessage(
            myself,
            village.day.latestDay().id,
            MessageContent.invoke(CDef.MessageType.アクション.code(), text, null)
        )
        val registered = messageService.registerMessage(village, message)
        // 通知
        val players = playerService.findPlayers(village.id)
        val charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        notificationService.notifyReceiveMessageToCustomerIfNeeded(village, players, registered)
        // IPアドレス更新
        accessInfoCoordinator.registerAccessInfo(village, myself, user.ipAddress!!)
    }

    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun creatorSay(village: Village, messageText: String) {
        val messageContent: MessageContent = MessageContent.invoke(CDef.MessageType.村建て発言.code(), messageText, null)
        // 発言できない状況ならエラー
        sayDomainService.assertCreatorSay(village, messageContent)
        // 発言
        val message: Message = Message.createCreatorSayMessage(messageText, village.day.latestDay().id)
        val registered = messageService.registerMessage(village, message)
        // 通知
        val players = playerService.findPlayers(village.id)
        val charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        notificationService.notifyReceiveMessageToCustomerIfNeeded(village, players, registered)
    }

    /**
     * 能力セット
     *
     * @param villageId villageId
     * @param user user
     * @param myselfId 行使村参加者ID (襲撃の場合のみ)
     * @param targetId 対象村参加者ID
     * @param abilityTypeCode 能力種別
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun setAbility(villageId: Int, user: FirewolfUser, myselfId: Int?, targetId: Int?, abilityTypeCode: String) {
        // 能力セットできない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val myself: VillageParticipant? = findParticipant(village, user)
        val villageAbilities: VillageAbilities = abilityService.findVillageAbilities(villageId)
        val abilityType = AbilityType(abilityTypeCode)
        abilityDomainService.assertAbility(village, myself, myselfId, targetId, abilityType, villageAbilities)
        // 能力セット
        val villageAbility = VillageAbility(village.day.latestDay().id, myselfId ?: myself!!.id, targetId, abilityType)
        abilityService.updateAbility(villageAbility)
        val charas: Charas = charachipService.findCharas(village.setting.charachip.charachipIds)
        val participant = myselfId?.let { village.participant.member(it) } ?: myself!!
        messageService.registerAbilitySetMessage(village, participant, targetId, abilityType, charas)
    }

    /**
     * 投票セット
     *
     * @param villageId villageId
     * @param user user
     * @param targetId 対象村参加者ID
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun setVote(villageId: Int, user: FirewolfUser, targetId: Int) {
        // 投票セットできない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        voteDomainService.assertVote(village, participant, targetId)
        // 投票
        val villageVote = VillageVote(
            village.day.latestDay().id,
            participant!!.id,
            targetId
        )
        voteService.updateVote(villageVote)
    }

    /**
     * コミットセット
     *
     * @param villageId villageId
     * @param user user
     * @param doCommit コミットするか
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun setCommit(villageId: Int, user: FirewolfUser, doCommit: Boolean) {
        // コミットできない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        commitDomainService.assertCommit(village, participant)
        // コミット
        val commit = Commit(village.day.latestDay().id, participant!!.id, doCommit)
        commitService.updateCommit(commit)
        messageService.registerCommitMessage(village, participant, doCommit)
        // 日付更新
        if (doCommit) dayChangeCoordinator.dayChangeIfNeeded(village)
    }

    /**
     * カミングアウトセット
     *
     * @param villageId villageId
     * @param user user
     * @param skills
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun setComingOut(villageId: Int, user: FirewolfUser, skills: Skills) {
        // カミングアウトできない状況ならエラー
        val village: Village = villageService.findVillage(villageId)
        val participant: VillageParticipant? = findParticipant(village, user)
        comingOutDomainService.assertComingOut(village, participant)
        // カミングアウト
        if (skills.list.isEmpty()) {
            comingOutService.deleteComingOut(participant!!.id)
        } else comingOutService.registerComingOut(
            participant!!.id,
            skills.list
        )
        messageService.registerComingOutMessage(village, participant, skills)
    }

    /**
     * 通知設定保存
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun registerNotification(
        villageId: Int,
        user: FirewolfUser,
        notificationCondition: VillageParticipantNotificationCondition
    ) {
        val village = villageService.findVillage(villageId)
        val participant = findParticipant(village, user) ?: return
        villageService.registerParticipantNotificationSetting(participant.id, notificationCondition)
    }

    /**
     * 参加状況や可能なアクションを取得
     * @param village village
     * @param user user
     * @param players players
     * @param charas charas
     */
    fun findActionSituation(
        village: Village,
        user: FirewolfUser?,
        players: Players,
        charas: Charas
    ): SituationAsParticipant {
        val player: Player? = if (user == null) null else playerService.findPlayer(user)
        val participant: VillageParticipant? = findParticipant(village, user)
        val skillRequest: SkillRequest? =
            if (participant == null) null else village.findMemberById(participant.id)!!.skillRequest
        val abilities: VillageAbilities = abilityService.findVillageAbilities(village.id)
        val votes: VillageVotes = voteService.findVillageVotes(village.id)
        val commit: Commit? = commitService.findCommit(village, participant)
        val latestDayMessageCountMap =
            messageService.findParticipateDayMessageList(village.id, village.day.latestDay(), participant)
        val charachips = charachipService.findCharachips(village.setting.charachip.charachipIds)

        return SituationAsParticipant(
            participate = participateDomainService.convertToSituation(
                village, participant, player, charas
            ),
            skillRequest = skillRequestDomainService.convertToSituation(village, participant, skillRequest),
            commit = commitDomainService.convertToSituation(village, participant, commit),
            comingOut = comingOutDomainService.convertToSituation(village, participant),
            say = sayDomainService.convertToSituation(village, player, participant, charas, latestDayMessageCountMap),
            rp = rpDomainService.convertToSituation(village, participant, charachips, charas),
            ability = abilityDomainService.convertToSituationList(village, participant, abilities),
            vote = voteDomainService.convertToSituation(village, participant, votes),
            creator = creatorDomainService.convertToSituation(village, player),
            admin = adminDomainService.convertToSituation(village, participant, players)
        )
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun registerVillage(paramVillage: Village): Village {
        // 村を登録
        val village: Village = villageService.registerVillage(paramVillage)
        // 村作成時のシステムメッセージを登録
        messageService.registerInitialMessage(village)
        // ダミーキャラを参加させる
        participateDummyChara(
            villageId = village.id,
            village = village,
            message = paramVillage.setting.charachip.dummyCharaDay0Message
        )
        return village
    }

    private fun participateDummyChara(
        villageId: Int,
        village: Village,
        message: String
    ) {
        val dummyPlayerId = 1 // 固定
        this.participate(
            villageId = villageId,
            playerId = dummyPlayerId,
            charaId = village.setting.charachip.dummyCharaId,
            charaShortName = village.setting.charachip.dummyCharaShortName,
            charaName = village.setting.charachip.dummyCharaName,
            message = message,
            isSpectate = false,
            ipAddress = "dummy"
        )
    }

    private fun assertSay(
        villageId: Int,
        user: FirewolfUser?,
        messageContent: MessageContent,
        targetId: Int?
    ) {
        val village: Village = villageService.findVillage(villageId)
        val player = user?.let { playerService.findPlayer(it) } ?: throw FirewolfBusinessException("発言できません")
        val participant = findParticipant(village, user) ?: throw FirewolfBusinessException("発言できません")
        val chara = charachipService.findChara(participant.charaId)
        val latestDayMessageCountMap =
            messageService.findParticipateDayMessageList(villageId, village.day.latestDay(), participant)
        sayDomainService.assertSay(village, participant, player, chara, latestDayMessageCountMap, messageContent)
    }

    fun saveNotification(
        villageId: Int,
        user: FirewolfUser,
        notificationCondition: VillageParticipantNotificationCondition
    ) {
        val village = villageService.findVillage(villageId)
        val myself = findParticipant(village, user) ?: throw FirewolfBusinessException("保存できません")
        villageService.registerParticipantNotificationSetting(myself.id, notificationCondition)
        if (notificationCondition.discordWebhookUrl.isNotEmpty() &&
            myself.notification?.discordWebhookUrl != notificationCondition.discordWebhookUrl
        ) {
            notificationService.notifyTest(notificationCondition.discordWebhookUrl, village.id)
        }
    }
}
