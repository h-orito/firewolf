package com.ort.firewolf.api.body

import jakarta.validation.constraints.NotNull

data class VillageActionBody(
    @field:NotNull(message = "発言内容は必須")
    val myself: String? = null,

    val target: String? = null,

    @field:NotNull(message = "発言内容は必須")
    val message: String? = null
)
