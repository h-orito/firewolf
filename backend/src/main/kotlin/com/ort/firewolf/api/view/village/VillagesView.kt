package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.Villages

data class VillagesView(
    val list: List<SimpleVillageView>
) {
    constructor(
        villages: Villages
    ) : this(
        list = villages.list.map { SimpleVillageView(it) }
    )
}
