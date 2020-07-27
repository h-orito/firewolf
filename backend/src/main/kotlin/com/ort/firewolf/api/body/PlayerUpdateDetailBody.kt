package com.ort.firewolf.api.body

data class PlayerUpdateDetailBody(
    val otherSiteName: String?,
    val introduction: String?
) {
    constructor() : this(null, null)
}
