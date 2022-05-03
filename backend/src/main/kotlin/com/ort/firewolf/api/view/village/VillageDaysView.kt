package com.ort.firewolf.api.view.village

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.village.VillageDays

data class VillageDaysView(
    val dayList: List<VillageDayView>
) {

    constructor(
        villageDays: VillageDays,
        silentHours: Int?
    ): this(
        dayList = villageDays.dayList.map {
            VillageDayView(it, silentHours)
        }
    )
}
