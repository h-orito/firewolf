package com.ort.firewolf.api.body

import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class VillageRegisterBody(
    @field:NotNull
    @get:Size(max = 40)
    val villageName: String?,

    @field:NotNull
    @Valid
    val setting: VillageSettingRegisterBody?
) {
    constructor() : this(null, null)
}

data class VillageSettingRegisterBody(
    @field:NotNull
    @Valid
    val time: VillageTimeCreateBody?,

    @field:NotNull
    @Valid
    val organization: VillageOrganizationCreateBody?,

    @field:NotNull
    @Valid
    val charachip: VillageCharachipCreateBody?,

    @field:NotNull
    @Valid
    val rule: VillageRuleCreateBody?,

    @field:NotNull
    @Valid
    val tags: VillageTagCreateBody?
) {
    constructor() : this(null, null, null, null, null)
}

data class VillageTimeCreateBody(
    @field:NotNull
    val startDatetime: LocalDateTime?,

    val silentHours: Int?
) {
    constructor() : this(null, null)
}

data class VillageOrganizationCreateBody(
    @field:NotNull
    val organization: String?
) {
    constructor() : this(null)
}

data class VillageCharachipCreateBody(
    @field:NotNull
    val dummyCharaId: Int?,

    @field:NotNull
    val dummyCharaShortName: String?,

    @field:NotNull
    val dummyCharaName: String?,

    @field:NotNull
    @field:Length(min = 1, max = 1000)
    val dummyCharaDay0Message: String?,

    @field:Length(min = 1, max = 1000)
    val dummyCharaDay1Message: String?,

    @field:NotNull
    val charachipIds: List<Int>?
) {
    constructor() : this(null, null, null, null, null, null)
}

data class VillageRuleCreateBody(
    @field:NotNull
    val openVote: Boolean?,

    @field:NotNull
    val availableSkillRequest: Boolean?,

    @field:NotNull
    val availableSpectate: Boolean?,

    @field:NotNull
    val openSkillInGrave: Boolean?,

    @field:NotNull
    val visibleGraveMessage: Boolean?,

    @field:NotNull
    val availableSuddenlyDeath: Boolean?,

    @field:NotNull
    val availableCommit: Boolean?,

    @field:NotNull
    val availableDummySkill: Boolean?,

    @field:NotNull
    val availableAction: Boolean?,

    @field:NotNull
    val availableSecretSay: Boolean?,

    @field:NotNull
    val availableGuardSameTarget: Boolean?,

    @field:NotNull
    @Valid
    val restrictList: List<VillageMessageRestrictCreateBody>?,

    @get:Size(max = 20)
    val joinPassword: String?
) {
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}

data class VillageTagCreateBody(
    @field:NotNull
    val list: List<String>?
) {
    constructor() : this(null)
}

data class VillageMessageRestrictCreateBody(
    @field:NotNull
    val type: String?,

    @field:NotNull
    @field:Max(1000, message = "回数は1000回以下にしてください")
    val count: Int?,

    @field:NotNull
    @field:Max(1000, message = "文字数は1000以下にしてください")
    val length: Int?
) {
    constructor() : this(null, null, null)
}