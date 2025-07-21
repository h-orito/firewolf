package com.ort.firewolf.api.body

import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.NotNull

data class VillageNotificationBody(
    @field:NotNull
    val webhookUrl: String? = null,
    val villageStart: Boolean? = null,
    val villageDaychange: Boolean? = null,
    val villageEpilogue: Boolean? = null,
    val secretSay: Boolean? = null,
    val abilitySay: Boolean? = null,
    val anchorSay: Boolean? = null,
    @field:Length(max = 30)
    val keyword: String? = null
)