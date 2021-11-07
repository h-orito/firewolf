package com.ort.firewolf.domain.model.myself.participant

data class VillageSayRestrictSituation(
    val restricted: Boolean,
    val maxCount: Int?,
    val remainingCount: Int?,
    val maxLength: Int,
    val maxLine: Int
)
