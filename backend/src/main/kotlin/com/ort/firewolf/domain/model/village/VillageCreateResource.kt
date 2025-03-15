package com.ort.firewolf.domain.model.village

import com.ort.firewolf.domain.model.message.MessageType
import java.time.LocalDateTime

data class VillageCreateResource(
    val villageName: String,
    val createPlayerId: Int,
    val setting: VillageSettingCreateResource
)

data class VillageSettingCreateResource(
    val time: VillageTimeCreateResource,
    val organization: VillageOrganizationCreateResource,
    val charachip: VillageCharachipCreateResource,
    val rule: VillageRuleCreateResource,
    val tags: VillageTagCreateResource,
)

data class VillageTimeCreateResource(
    val startDatetime: LocalDateTime,
    val silentHours: Int?
)

data class VillageOrganizationCreateResource(
    val organization: String
)

data class VillageCharachipCreateResource(
    val dummyCharaId: Int,
    val dummyCharaShortName: String,
    val dummyCharaName: String,
    val dummyCharaDay0Message: String,
    val dummyCharaDay1Message: String?,
    val charachipIds: List<Int>
)

data class VillageRuleCreateResource(
    val isOpenVote: Boolean,
    val isAvailableSkillRequest: Boolean,
    val isAvailableSpectate: Boolean,
    val isOpenSkillInGrave: Boolean,
    val isVisibleGraveMessage: Boolean,
    val isAvailableSuddenlyDeath: Boolean,
    val isAvailableCommit: Boolean,
    val isAvailableDummySkill: Boolean,
    val isAvailableAction: Boolean,
    val isAvailableSecretSay: Boolean,
    val isAvailableGuardSameTarget: Boolean,
    val restrictList: List<VillageMessageRestrictCreateResource>,
    val joinPassword: String?
)

data class VillageTagCreateResource(
    val tagCodes: List<String>
)

data class VillageMessageRestrictCreateResource(
    val type: MessageType,
    val count: Int,
    val length: Int
)