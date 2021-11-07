package com.ort.firewolf.domain.service.village

import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageCharachipCreateResource
import com.ort.firewolf.domain.model.village.VillageCreateResource
import com.ort.firewolf.domain.model.village.VillageMessageRestrictCreateResource
import com.ort.firewolf.domain.model.village.VillageOrganizationCreateResource
import com.ort.firewolf.domain.model.village.VillageRuleCreateResource
import com.ort.firewolf.domain.model.village.VillageTimeCreateResource
import com.ort.firewolf.domain.model.village.setting.VillageCharachip
import com.ort.firewolf.domain.model.village.setting.VillageMessageRestricts
import com.ort.firewolf.domain.model.village.setting.VillageOrganizations
import com.ort.firewolf.domain.model.village.setting.VillageRules
import com.ort.firewolf.domain.model.village.setting.VillageTime
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class VillageSettingDomainService {

    fun assertModify(
        village: Village,
        resource: VillageCreateResource
    ) {
        assertOrganization(village.participant.count, resource.setting.organization)
        assertCharachip(village.setting.charachip, resource.setting.charachip)
        assertRule(village, resource.setting.rule)
    }

    fun createModifyMessage(village: Village, resource: VillageCreateResource): Message? {
        // "村の設定が変更されました。\n変更項目"
        val list = mutableListOf<String>()
        if (village.name != resource.villageName) list.add("村の名前")
        village.setting.let { setting ->
            addTimeModifyMessage(list, setting.time, resource.setting.time)
            addOrganizationModifyMessage(list, setting.organizations, resource.setting.organization)
            addRuleModifyMessage(list, setting.rules, resource.setting.rule)
            addRestrictModifyMessage(list, setting.rules.messageRestrict, resource.setting.rule.restrictList)
            addPasswordModifyMessage(list, setting.password.joinPassword, resource.setting.rule.joinPassword)
        }
        val message = list.map { "・${it}" }.joinToString(
            separator = "\n",
            prefix = "村の設定が変更されました。\n変更項目\n"
        )
        if (list.isEmpty()) return null
        return Message.createPublicSystemMessage(
            text = message,
            villageDayId = village.day.latestDay().id
        )
    }

    private fun addTimeModifyMessage(
        list: MutableList<String>,
        time: VillageTime,
        resourceTime: VillageTimeCreateResource
    ) {
        if (time.startDatetime != resourceTime.startDatetime) list.add("開始日時")
        if (time.silentHours != resourceTime.silentHours) list.add("更新後沈黙時間")
    }

    private fun addOrganizationModifyMessage(
        list: MutableList<String>,
        organizations: VillageOrganizations,
        resourceOrganization: VillageOrganizationCreateResource
    ) {
        val org = organizations.toString()
        val resourceOrg = VillageOrganizations(resourceOrganization.organization).toString()
        if (org != resourceOrg) list.add("編成")
    }

    private fun addRuleModifyMessage(
        list: MutableList<String>,
        rules: VillageRules,
        resourceRules: VillageRuleCreateResource
    ) {
        if (rules.openVote != resourceRules.isOpenVote) list.add("投票形式")
        if (!rules.availableSkillRequest && resourceRules.isAvailableSkillRequest) list.add("役職希望有無")
        if (rules.availableSkillRequest && !resourceRules.isAvailableSkillRequest) list.add("役職希望有無（全員おまかせに変更されます）")
        if (rules.availableSpectate != resourceRules.isAvailableSpectate) list.add("見物人有無")
        if (rules.openSkillInGrave != resourceRules.isOpenSkillInGrave) list.add("墓下役職公開有無")
        if (rules.visibleGraveMessage != resourceRules.isVisibleGraveMessage) list.add("墓下発言公開有無")
        if (rules.availableSuddenlyDeath != resourceRules.isAvailableSuddenlyDeath) list.add("突然死有無")
        if (rules.availableCommit != resourceRules.isAvailableCommit) list.add("時短希望有無")
        if (rules.availableDummySkill != resourceRules.isAvailableDummySkill) list.add("ダミー役欠け有無")
        if (rules.availableAction != resourceRules.isAvailableAction) list.add("アクション有無")
        if (rules.availableSecretSay != resourceRules.isAvailableSecretSay) list.add("秘話有無")
    }

    private fun addRestrictModifyMessage(
        list: MutableList<String>,
        restricts: VillageMessageRestricts,
        resourceRestrictList: List<VillageMessageRestrictCreateResource>
    ) {
        val existsDiff = restricts.restrictList.any { restrict ->
            val resourceRestrict = resourceRestrictList.firstOrNull { it.type.code == restrict.type.code }
            resourceRestrict == null || restrict.count != resourceRestrict.count || restrict.length != resourceRestrict.length
        }
        if (existsDiff) list.add("発言制限")
    }

    private fun addPasswordModifyMessage(
        list: MutableList<String>,
        password: String?,
        resourcePassword: String?
    ) {
        if (password.isNullOrEmpty() && resourcePassword.isNullOrEmpty()) return
        if (password != resourcePassword) list.add("参加パスワード")
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun assertOrganization(participantCount: Int, organization: VillageOrganizationCreateResource) {
        // 現在の人数が編成の上限人数を超えていたらNG
        val resourceOrg = VillageOrganizations.invoke(organization.organization)
        val capacity = resourceOrg.organization.keys.max()!!
        if (capacity < participantCount) {
            throw FirewolfBusinessException("現在の参加人数（${participantCount}人）が定員を超えています")
        }
    }

    private fun assertCharachip(charachip: VillageCharachip, resourceCharachip: VillageCharachipCreateResource) {
        if (charachip.charachipId != resourceCharachip.charachipId
            || charachip.dummyCharaId != resourceCharachip.dummyCharaId
        ) {
            throw FirewolfBusinessException("キャラチップとダミーキャラは変更できません")
        }
    }

    private fun assertRule(village: Village, rule: VillageRuleCreateResource) {
        if (village.spectator.count > 0 && !rule.isAvailableSpectate) {
            throw FirewolfBusinessException("すでに見物人がいるため見物人はOFFにできません")
        }
    }
}