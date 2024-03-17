package com.ort.firewolf.application.coordinator

import com.ort.firewolf.application.service.NotificationService
import com.ort.firewolf.application.service.PlayerService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.springframework.stereotype.Service

@Service
class AccessInfoCoordinator(
    private val villageService: VillageService,
    private val playerService: PlayerService,
    private val notificationService: NotificationService,
) {

    fun registerAccessInfo(
        village: Village,
        myself: VillageParticipant,
        ipAddress: String
    ) {
        val changedVillage: Village = village.addParticipantIpAddress(myself.id, ipAddress)
        val savedVillage = villageService.updateVillageDifference(village, changedVillage)
        // IPアドレスが重複している人がいたら通知
        if (!playerService.findPlayer(myself.playerId!!).shouldCheckAccessInfo) return

        val isContain = village.allParticipants().memberList
            .filterNot { it.id == myself.id || it.playerId == 1 }
            .flatMap { it.ipAddresses }.distinct()
            .contains(ipAddress)
        if (isContain) {
            notificationService.notifyToDeveloperTextIfNeeded(village, "IPアドレス重複検出: $ipAddress")
        }
    }
}