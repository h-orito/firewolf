package com.ort.firewolf.domain.service.daychange

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.daychange.DayChange
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.service.ability.AbilityDomainService
import com.ort.firewolf.domain.service.skill.SkillAssignDomainService
import com.ort.firewolf.fw.FirewolfDateUtil
import org.springframework.stereotype.Service

@Service
class PrologueDomainService(
    private val abilityDomainService: AbilityDomainService,
    private val skillAssignDomainService: SkillAssignDomainService
) {
    fun extendIfNeeded(dayChange: DayChange): DayChange {
        // 開始時刻になっていない場合は何もしない
        if (!shouldForward(dayChange.village)) return dayChange
        // 参加人数が足りている場合は何もしない
        if (!isNotEnoughMemberCount(dayChange.village)) return dayChange
        // 延長
        return extendPrologue(dayChange).setIsChange(dayChange)
    }

    fun addDayIfNeeded(
        dayChange: DayChange
    ): DayChange {
        // 開始時刻になっていない場合は何もしない
        if (!shouldForward(dayChange.village)) return dayChange
        // 新しい日付追加
        return dayChange.copy(village = dayChange.village.addNewDay()).setIsChange(dayChange)
    }

    fun dayChange(
        beforeDayChange: DayChange,
        charas: Charas
    ): DayChange {
        // 開始メッセージ追加
        var dayChange = addStartMessage(beforeDayChange)
        // 役職割り当て
        dayChange = skillAssignDomainService.assign(dayChange)
        // 役職構成メッセージ追加
        dayChange = addOrganizationMessage(dayChange)
        // 仲間把握メッセージ追加
        dayChange = abilityDomainService.addRecongnizeMessages(dayChange, charas)
        // ステータス変更
        dayChange = dayChange.copy(village = dayChange.village.changeStatus(CDef.VillageStatus.進行中))
        // デフォルト能力行使指定
        dayChange = abilityDomainService.addDefaultAbilities(dayChange)
        // ダミーキャラ発言
        dayChange = addDummyCharaFirstDayMessageIfNeeded(dayChange, charas)

        return dayChange.setIsChange(beforeDayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // 日付を進める必要があるか
    private fun shouldForward(village: Village): Boolean =
        !FirewolfDateUtil.currentLocalDateTime().isBefore(village.day.latestDay().dayChangeDatetime)

    private fun extendPrologue(dayChange: DayChange): DayChange {
        return dayChange.copy(
            village = dayChange.village.extendPrologue(),
            messages = dayChange.messages.add(dayChange.village.createExtendPrologueMessage())
        )
    }

    private fun isNotEnoughMemberCount(village: Village) =
        village.participant.memberList.count { !it.isGone } < village.setting.capacity.min

    private fun addStartMessage(dayChange: DayChange): DayChange {
        return dayChange.copy(
            messages = dayChange.messages.add(dayChange.village.createVillageDay1Message())
        )
    }

    private fun addOrganizationMessage(dayChange: DayChange): DayChange {
        return dayChange.copy(
            messages = dayChange.messages.add(dayChange.village.createOrganizationMessage())
        )
    }

    private fun addDummyCharaFirstDayMessageIfNeeded(dayChange: DayChange, charas: Charas): DayChange {
        return dayChange.village.createDummyCharaFirstDayMessage(charas)?.let {
            dayChange.copy(
                messages = dayChange.messages.add(it)
            )
        } ?: dayChange
    }
}