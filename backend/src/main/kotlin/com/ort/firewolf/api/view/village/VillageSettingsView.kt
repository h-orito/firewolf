package com.ort.firewolf.api.view.village

import com.ort.firewolf.domain.model.village.setting.PersonCapacity
import com.ort.firewolf.domain.model.village.setting.VillageCharachip
import com.ort.firewolf.domain.model.village.setting.VillageOrganizations
import com.ort.firewolf.domain.model.village.setting.VillageRules
import com.ort.firewolf.domain.model.village.setting.VillageSettings
import com.ort.firewolf.domain.model.village.setting.VillageTags

data class VillageSettingsView(
    val capacity: PersonCapacity,
    val time: VillageTimeView,
    val charachip: VillageCharachip,
    val organizations: VillageOrganizations,
    val rules: VillageRules,
    val tags: VillageTags,
    val password: VillagePasswordView
) {
    constructor(
        villageSettings: VillageSettings
    ) : this(
        capacity = villageSettings.capacity,
        time = VillageTimeView(villageSettings.time),
        charachip = villageSettings.charachip,
        organizations = villageSettings.organizations,
        rules = villageSettings.rules,
        tags = villageSettings.tags,
        password = VillagePasswordView(villageSettings.password)
    )
}
