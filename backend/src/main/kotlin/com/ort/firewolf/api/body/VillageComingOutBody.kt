package com.ort.firewolf.api.body

data class VillageComingOutBody(
    val skillCode: List<String>?
) {
    constructor() : this(null)
}
