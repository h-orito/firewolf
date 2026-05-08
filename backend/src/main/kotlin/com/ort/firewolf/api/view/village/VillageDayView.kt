package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.VillageDay
import java.time.LocalDateTime
import java.time.LocalTime

data class VillageDayView(
    val id: Int,
    val day: Int,
    val noonnight: String,
    val startDatetime: LocalDateTime,
    val dayChangeDatetime: LocalDateTime,
    val sayableStartTime: LocalTime,
) {
    constructor(
        villageDay: VillageDay,
        silentHoursDay1: Int?,
        silentHoursDay2: Int?,
    ) : this(
        id = villageDay.id,
        day = villageDay.day,
        noonnight = villageDay.noonnight,
        startDatetime = villageDay.startDatetime,
        dayChangeDatetime = villageDay.dayChangeDatetime,
        sayableStartTime =
            silentHoursOf(villageDay.day, silentHoursDay1, silentHoursDay2).let { silentHours ->
                if (silentHours == null) {
                    villageDay.startDatetime.toLocalTime()
                } else {
                    villageDay.startDatetime.plusHours(silentHours.toLong()).toLocalTime()
                }
            },
    )

    companion object {
        private fun silentHoursOf(
            day: Int,
            silentHoursDay1: Int?,
            silentHoursDay2: Int?,
        ): Int? = if (day <= 1) silentHoursDay1 else silentHoursDay2
    }
}
