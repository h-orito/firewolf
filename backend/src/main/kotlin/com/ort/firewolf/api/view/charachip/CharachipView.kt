package com.ort.firewolf.api.view.charachip

import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charachip
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.charachip.Designer

data class CharachipView(
    val id: Int,
    val name: String,
    val designer: Designer,
    val descriptionUrl: String,
    val charaList: List<Chara> // domainとの違い
) {

    constructor(
        charachip: Charachip,
        charas: Charas
    ) : this(
        id = charachip.id,
        name = charachip.name,
        designer = charachip.designer,
        descriptionUrl = charachip.descriptionUrl,
        charaList = charas.list.filter { it.charachipId == charachip.id }
    )
}