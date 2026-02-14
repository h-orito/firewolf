package com.ort.firewolf.api.view.player

import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.player.PlayerRecords
import com.ort.firewolf.domain.model.player.Players
import com.ort.firewolf.domain.model.player.record.CampRecord
import com.ort.firewolf.domain.model.player.record.Record
import com.ort.firewolf.domain.model.player.record.SkillRecord

data class PlayerRecordsView(
    val player: PlayerView,
    val wholeRecord: Record,
    val campRecordList: List<CampRecord>,
    val skillRecordList: List<SkillRecord>,
    val participateVillageList: List<ParticipateVillageView>
) {
    constructor(
        playerRecords: PlayerRecords,
        charas: Charas,
        players: Players,
        createPlayers: Players
    ) : this(
        player = PlayerView(playerRecords.player),
        wholeRecord = playerRecords.wholeRecord,
        campRecordList = playerRecords.campRecordList,
        skillRecordList = playerRecords.skillRecordList,
        participateVillageList = playerRecords.participateVillageList.map { participateVillage ->
            ParticipateVillageView(
                participateVillage.village,
                participateVillage.participant,
                Charas(charas.list.filter { participateVillage.participant.charaId == it.id }),
                Players(players.list.filter { player ->
                    participateVillage.village.participant.memberList.any { member -> member.playerId == player.id }
                            || participateVillage.village.spectator.memberList.any { member -> member.playerId == player.id }
                }),
                createPlayers.list.first { it.id == participateVillage.village.creatorPlayerId }
            )
        }
    )
}
