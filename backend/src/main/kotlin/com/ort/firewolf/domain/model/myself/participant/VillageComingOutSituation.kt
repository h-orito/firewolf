package com.ort.firewolf.domain.model.myself.participant

import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.village.participant.comingout.ComingOuts

data class VillageComingOutSituation(
    val availableComingOut: Boolean,
    val currentComingOuts: ComingOuts,
    val selectableSkillList: List<Skill>,
)
