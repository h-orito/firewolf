package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.setting.VillagePassword

data class VillagePasswordView(
    val joinPasswordRequired: Boolean
) {
    constructor(
        villagePassword: VillagePassword
    ) : this(
        joinPasswordRequired = villagePassword.joinPasswordRequired
    )
}