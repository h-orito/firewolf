package com.ort.firewolf.api.body

import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.NotNull

data class VillageParticipateBody(
    @field:NotNull(message = "charaIdは必須")
    val charaId: Int?,

    @field:NotNull(message = "charaShortNameは必須")
    @field:Length(min = 1, max = 1)
    val charaShortName: String?,

    @field:NotNull(message = "charaNameは必須")
    @field:Length(min = 1, max = 40)
    val charaName: String?,

    @field:NotNull(message = "firstRequestSkillは必須")
    val firstRequestSkill: String?,

    @field:NotNull(message = "secondRequestSkillは必須")
    val secondRequestSkill: String?,

    @field:NotNull(message = "joinMessageは必須")
    val joinMessage: String?,

    val joinPassword: String?,

    val spectator: Boolean?
) {
    constructor() : this(null, null, null, null, null, null, null, null) {}
}
