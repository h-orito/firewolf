package com.ort.firewolf.application.service

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.skill.Skills
import com.ort.firewolf.domain.model.skill.toModel
import org.springframework.stereotype.Service

@Service
class SkillService {

    fun findSkills(): Skills {
        return Skills(
            CDef.Skill.listAll().filterNot {
                it.isSomeoneSkill
            }.sortedBy {
                it.order().toInt()
            }.map { it.toModel() }
        )
    }
}
