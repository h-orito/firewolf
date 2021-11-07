package com.ort.firewolf.domain.model.myself.participant

import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.skill.SkillRequest

data class VillageSkillRequestSituation(
    val availableSkillRequest: Boolean,
    val selectableSkillList: List<Skill> = listOf(),
    val skillRequest: SkillRequest?
)
