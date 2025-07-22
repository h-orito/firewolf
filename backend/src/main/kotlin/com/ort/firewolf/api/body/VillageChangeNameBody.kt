package com.ort.firewolf.api.body

import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.NotNull

data class VillageChangeNameBody(
    @field:NotNull
    @field:Length(max = 40)
    val name: String?,

    @field:NotNull
    @field:Length(max = 1)
    val shortName: String?
) {
    constructor() : this(null, null) {}
}
