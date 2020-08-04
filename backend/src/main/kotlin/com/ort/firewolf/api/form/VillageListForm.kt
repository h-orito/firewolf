package com.ort.firewolf.api.form

data class VillageListForm(
    val village_status: List<String>?
) {
    constructor() : this(null)
}
