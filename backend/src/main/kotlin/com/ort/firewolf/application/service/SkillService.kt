package com.ort.firewolf.application.service

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.skill.Skills
import org.springframework.stereotype.Service

@Service
class SkillService {

    fun findSkills(): Skills {
        return Skills(
            CDef.Skill.listAll().filterNot {
                CDef.Skill.listOfSomeoneSkill().contains(it)
            }.sortedBy {
                it.order()
            }.map { Skill(it) }
        )
    }
}
