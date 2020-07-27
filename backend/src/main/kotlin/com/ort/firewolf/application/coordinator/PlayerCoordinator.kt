package com.ort.firewolf.application.coordinator

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.player.PlayerRecords
import com.ort.firewolf.domain.model.village.Villages
import org.springframework.stereotype.Service


@Service
class PlayerCoordinator(
    private val villageService: VillageService
) {
    fun findPlayerRecords(player: Player): PlayerRecords {
        if (player.participateFinishedVillageIdList.isEmpty()) return PlayerRecords(player, Villages(listOf()))
        val villages = villageService.findVillagesAsDetail(player.participateFinishedVillageIdList)
        return PlayerRecords(player, villages.copy(
            list = villages.list.filter { it.status.toCdef() != CDef.VillageStatus.廃村 }
        ))
    }
}
