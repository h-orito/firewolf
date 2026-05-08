package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.VillageDays

data class VillageDaysView(
    val dayList: List<VillageDayView>,
) {
    constructor(
        villageDays: VillageDays,
        silentHoursDay1: Int?,
        silentHoursDay2: Int?,
    ) : this(
        dayList =
            villageDays.dayList.map {
                VillageDayView(it, silentHoursDay1, silentHoursDay2)
            },
    )
}
