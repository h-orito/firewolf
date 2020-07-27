package com.ort.firewolf.api.form

data class VillageRecordListForm(
    val vid: List<Int>?
) {
    constructor() : this(null)
}
