package com.ort.firewolf.api.body

import jakarta.validation.constraints.NotNull

data class CreatorKickBody(
    @field:NotNull
    val targetId: Int?
) {
    constructor() : this(null)
}
