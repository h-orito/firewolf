package com.ort.firewolf.domain.service.admin

import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.myself.participant.VillageAdminSituation
import com.ort.firewolf.domain.model.myself.participant.VillageParticipantSituation
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class AdminDomainService {

    fun convertToSituation(village: Village, myself: VillageParticipant?, players: Players, charas: Charas): VillageAdminSituation {
        if (myself?.playerId != 1) return VillageAdminSituation(false, listOf())
        return VillageAdminSituation(
            isAdmin = true,
            participantList = village.participant.memberList.map { participant ->
                VillageParticipantSituation(
                    name = charas.chara(participant.charaId).charaName.fullName(),
                    nickname = players.list.find { it.id == participant.playerId }!!.nickname,
                    twitterUserName = players.list.find { it.id == participant.playerId }!!.twitterUserName,
                    skillName = participant.skill?.name
                )
            }
        )
    }
}