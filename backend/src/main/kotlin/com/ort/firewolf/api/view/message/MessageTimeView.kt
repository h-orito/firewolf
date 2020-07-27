package com.ort.firewolf.api.view.message

import com.ort.firewolf.domain.model.message.MessageTime
import com.ort.firewolf.domain.model.village.VillageDay
import java.time.LocalDateTime

data class MessageTimeView(
    val villageDayId: Int,
    val day: Int,
    val datetime: LocalDateTime,
    val unixTimeMilli: Long
) {
    constructor(
        messageTime: MessageTime,
        villageDay: VillageDay
    ) : this(
        villageDayId = messageTime.villageDayId,
        day = villageDay.day,
        datetime = messageTime.datetime,
        unixTimeMilli = messageTime.unixTimeMilli
    )
}
