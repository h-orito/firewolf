package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.setting.VillageTime
import java.time.LocalDateTime
import java.time.LocalTime

data class VillageTimeView(
    val termType: String,
    val startDatetime: LocalDateTime,
    val dayChangeIntervalSeconds: Int,
    val silentHoursDay1: Int?,
    val silentHoursDay2: Int?,
    val sayableStart: LocalTime,
    val sayableEnd: LocalTime,
) {
    constructor(
        villageTime: VillageTime,
    ) : this(
        termType = villageTime.termType,
        startDatetime = villageTime.startDatetime,
        dayChangeIntervalSeconds = villageTime.dayChangeIntervalSeconds,
        silentHoursDay1 = villageTime.silentHoursDay1,
        silentHoursDay2 = villageTime.silentHoursDay2,
        sayableStart =
            if (villageTime.silentHoursDay1 == null) {
                villageTime.startDatetime.toLocalTime()
            } else {
                villageTime.startDatetime.plusHours(villageTime.silentHoursDay1.toLong()).toLocalTime()
            },
        sayableEnd = villageTime.startDatetime.toLocalTime(),
    )
}
