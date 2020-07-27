package com.ort.firewolf.api.view.reserved

import com.ort.firewolf.domain.model.reserved.ReservedVillages

data class ReservedVillagesView(
    val list: List<ReservedVillageView>
) {
    constructor(
        reservedVillages: ReservedVillages
    ) : this(
        list = reservedVillages.list.map { ReservedVillageView(it) }
    )
}