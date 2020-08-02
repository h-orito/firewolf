package com.ort.firewolf.api.body

import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.NotNull

data class VillageRegisterBody(
    @field:NotNull
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
    val rule: VillageRuleCreateBody?
) {
    constructor() : this(null, null, null, null)
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
    val charachipId: Int?
) {
    constructor() : this(null, null)
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
    @Valid
    val restrictList: List<VillageMessageRestrictCreateBody>?,

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
        null
    )
}

data class VillageMessageRestrictCreateBody(
    @field:NotNull
    val type: String?,

    @field:NotNull
    @field:Max(100)
    val count: Int?,

    @field:NotNull
    @field:Max(200)
    val length: Int?
) {
    constructor() : this(null, null, null)
}