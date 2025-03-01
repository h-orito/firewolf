package com.ort.firewolf.application.coordinator

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.application.service.AbilityService
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.application.service.CommitService
import com.ort.firewolf.application.service.MessageService
import com.ort.firewolf.application.service.NotificationService
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.application.service.VoteService
import com.ort.firewolf.domain.model.commit.Commits
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.MessageQuery
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.vote.VillageVotes
import com.ort.firewolf.domain.service.daychange.DayChangeDomainService
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DayChangeCoordinator(
    val villageService: VillageService,
    val voteService: VoteService,
    val abilityService: AbilityService,
    val commitService: CommitService,
    val messageService: MessageService,
    val charachipService: CharachipService,
    val playerService: PlayerService,
    val notificationService: NotificationService,
    // domain service
    val dayChangeDomainService: DayChangeDomainService,
    // other
    val cacheManager: CacheManager
) {

    /**
     * 必要あれば日付更新
     *
     * @param village village
     */
    @Transactional(rollbackFor = [Exception::class, FirewolfBusinessException::class])
    fun dayChangeIfNeeded(village: Village) {
        val votes: VillageVotes = voteService.findVillageVotes(village.id)
        val abilities: VillageAbilities = abilityService.findVillageAbilities(village.id)
        val commits: Commits = commitService.findCommits(village.id)
        // 最新日の通常発言
        val todayMessages = messageService.findMessages(
            village.id,
            village.day.latestDay().id,
            MessageQuery(listOf(CDef.MessageType.通常発言))
        )
        val players: Players = playerService.findPlayers(village.id)

        val beforeDayChange = DayChange(
            village.copy(
                participant = village.participant.copy(
                    count = village.participant.memberList.count { !it.isGone },
                    memberList = village.participant.memberList.filter { !it.isGone }
                )
            ), votes, abilities, players)

        // プロローグ延長処理
        var dayChange = updateIfNeeded(
            beforeDayChange,
            dayChangeDomainService.extendVillageIfNeeded(beforeDayChange)
        )
        val shouldClearCache: Boolean = dayChange.isChange

        // 必要あれば日付追加
        dayChange = dayChangeDomainService.addDayIfNeeded(dayChange, commits).let {
            if (!it.isChange) {
                if (shouldClearCache) clearCache()
                return
            }
            updateIfNeeded(dayChange, it)
        }

        // 登録後の村日付idが必要になるので取得し直す
        dayChange = dayChange.copy(village = villageService.findVillageWithoutCache(village.id))

        // 日付更新
        dayChangeDomainService.process(dayChange, todayMessages, commits).also {
            updateIfNeeded(dayChange, it)
            notifyIfNeeded(beforeDayChange, it)
        }

        // キャッシュクリア
        clearCache()
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun updateIfNeeded(before: DayChange, after: DayChange): DayChange {
        if (!after.isChange) return after
        update(before, after)
        return after.copy(isChange = false)
    }

    private fun update(before: DayChange, after: DayChange) {
        // player
        if (before.players.existsDifference(after.players)) {
            playerService.updateDifference(before.players, after.players)
        }
        // village
        if (before.village.existsDifference(after.village)) {
            villageService.updateVillageDifference(before.village, after.village)
        }
        // message
        if (before.messages.existsDifference(after.messages)) {
            messageService.updateDifference(before.village, before.messages, after.messages)
        }
        // votes
        if (before.votes.existsDifference(after.votes)) {
            voteService.updateDifference(before.votes, after.votes)
        }
        // abilities
        if (before.abilities.existsDifference(after.abilities)) {
            abilityService.updateDifference(before.abilities, after.abilities)
        }
    }

    private fun notifyIfNeeded(current: DayChange, changed: DayChange) {
        if (current.village.status.isPrologue() && changed.village.status.isProgress()) {
            notificationService.notifyVillageStartToCustomerIfNeeded(changed.village)
        } else if (current.village.status.isProgress() && changed.village.status.isEpilogue()) {
            notificationService.notifyVillageEpilogueToCustomerIfNeeded(changed.village)
        } else if (
            !current.village.status.isEpilogue() &&
            current.village.day.latestDay().day != changed.village.day.latestDay().day
        ) {
            notificationService.notifyVillageDaychangeToCustomerIfNeeded(changed.village)
        }
    }

    private fun clearCache() {
        cacheManager.getCache("village")?.clear()
        cacheManager.getCache("messages")?.clear()
        cacheManager.getCache("latest-messages")?.clear()
    }
}