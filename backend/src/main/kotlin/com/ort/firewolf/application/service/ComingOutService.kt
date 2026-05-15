package com.ort.firewolf.application.service

import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.village.participant.comingout.ComingOut
import com.ort.firewolf.domain.model.village.participant.comingout.ComingOuts
import com.ort.firewolf.infrastructure.datasource.comingout.ComingOutDataSource
import org.springframework.stereotype.Service

@Service
class ComingOutService(
    private val comingOutDataSource: ComingOutDataSource,
) {
    fun registerComingOut(
        participantId: Int,
        skillList: List<Skill>,
    ) = comingOutDataSource.register(
        participantId,
        ComingOuts(list = skillList.map { ComingOut(it) }),
    )

    fun deleteComingOut(participantId: Int) = comingOutDataSource.delete(participantId)
}
