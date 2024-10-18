package com.ort.firewolf.domain.model.village.setting

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.village.VillageDays
import com.ort.firewolf.domain.model.village.VillageSettingCreateResource
import java.time.LocalDateTime

data class VillageSettings(
    val capacity: PersonCapacity,
    val time: VillageTime,
    val charachip: VillageCharachip,
    val organizations: VillageOrganizations,
    val rules: VillageRules,
    val tags: VillageTags,
    val password: VillagePassword
) {

    companion object {

        fun createForRegister(
            resource: VillageSettingCreateResource
        ): VillageSettings {
            val org = VillageOrganizations.invoke(resource.organization.organization)
            return VillageSettings(
                capacity = PersonCapacity(
                    min = org.organization.keys.min(),
                    max = org.organization.keys.max()
                ),
                time = VillageTime(
                    termType = CDef.Term.長期.code(),
                    prologueStartDatetime = LocalDateTime.now(),
                    epilogueDay = null,
                    epilogueStartDatetime = null,
                    startDatetime = resource.time.startDatetime,
                    dayChangeIntervalSeconds = 86400,
                    silentHours = resource.time.silentHours
                ),
                charachip = VillageCharachip(
                    dummyCharaId = resource.charachip.dummyCharaId,
                    dummyCharaShortName = resource.charachip.dummyCharaShortName,
                    dummyCharaName = resource.charachip.dummyCharaName,
                    dummyCharaDay0Message = resource.charachip.dummyCharaDay0Message,
                    dummyCharaDay1Message = resource.charachip.dummyCharaDay1Message,
                    charachipIds = resource.charachip.charachipIds
                ),
                organizations = org,
                rules = VillageRules(
                    openVote = resource.rule.isOpenVote,
                    availableSkillRequest = resource.rule.isAvailableSkillRequest,
                    availableSpectate = resource.rule.isAvailableSpectate,
                    openSkillInGrave = resource.rule.isOpenSkillInGrave,
                    visibleGraveMessage = resource.rule.isVisibleGraveMessage,
                    availableSuddenlyDeath = resource.rule.isAvailableSuddenlyDeath,
                    availableCommit = resource.rule.isAvailableCommit,
                    availableDummySkill = resource.rule.isAvailableDummySkill,
                    availableAction = resource.rule.isAvailableAction,
                    availableSecretSay = resource.rule.isAvailableSecretSay,
                    messageRestrict = VillageMessageRestricts(
                        existRestricts = true,
                        restrictList = resource.rule.restrictList.map {
                            VillageMessageRestrict(
                                type = it.type,
                                count = it.count,
                                length = it.length
                            )
                        }
                    )
                ),
                tags = VillageTags(list = resource.tags.tagCodes),
                password = VillagePassword(
                    joinPasswordRequired = !resource.rule.joinPassword.isNullOrEmpty(),
                    joinPassword = resource.rule.joinPassword
                )
            )
        }
    }

    fun existsDifference(setting: VillageSettings): Boolean {
        return capacity.existsDifference(setting.capacity)
                || charachip.existsDifference(setting.charachip)
                || time.existsDifference(setting.time)
                || organizations.existsDifference(setting.organizations)
                || rules.existsDifference(setting.rules)
                || tags.existsDifference(setting.tags)
                || password.existsDifference(setting.password)
    }

    fun extendPrologue(): VillageSettings {
        return this.copy(time = time.extendPrologue())
    }

    fun toEpilogue(villageDays: VillageDays): VillageSettings {
        return this.copy(
            time = time.toEpilogue(villageDays)
        )
    }
}