package com.ort.firewolf.domain.service.daychange

import com.ort.firewolf.domain.model.commit.Commits
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.message.Messages
import org.springframework.stereotype.Service

@Service
class DayChangeDomainService(
    private val prologueDomainService: PrologueDomainService,
    private val progressDomainService: ProgressDomainService,
    private val epilogueDomainService: EpilogueDomainService
) {
    fun extendVillageIfNeeded(dayChange: DayChange): DayChange {
        return if (!dayChange.village.status.isPrologue()) dayChange
        else prologueDomainService.extendIfNeeded(dayChange)
    }

    // コミットや時間経過で次の日に遷移させる場合は村日付を追加
    fun addDayIfNeeded(dayChange: DayChange, commits: Commits): DayChange {
        val status = dayChange.village.status
        return when {
            // プロローグ
            status.isPrologue() -> prologueDomainService.addDayIfNeeded(dayChange)
            // 進行中
            status.isProgress() -> progressDomainService.addDayIfNeeded(dayChange, commits)
            // エピローグ
            status.isEpilogue() -> epilogueDomainService.addDayIfNeeded(dayChange)
            // 終了後
            else -> dayChange
        }
    }

    // 日付変更処理
    fun process(dayChange: DayChange, todayMessages: Messages, commits: Commits): DayChange {
        val status = dayChange.village.status
        return when {
            // プロローグ
            status.isPrologue() -> prologueDomainService.dayChange(dayChange)
            // 進行中
            status.isProgress() -> progressDomainService.dayChange(dayChange, todayMessages, commits)
            // エピローグ
            status.isEpilogue() -> epilogueDomainService.dayChange(dayChange)
            // 終了後
            else -> dayChange
        }
    }
}