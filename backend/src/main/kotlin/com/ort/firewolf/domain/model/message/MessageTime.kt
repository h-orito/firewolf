package com.ort.firewolf.domain.model.message

import java.time.LocalDateTime

data class MessageTime(
    val villageDayId: Int,
    val datetime: LocalDateTime,
    val unixTimeMilli: Long
) {
}