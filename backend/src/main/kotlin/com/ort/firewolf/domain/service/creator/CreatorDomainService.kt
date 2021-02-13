package com.ort.firewolf.domain.service.creator

import com.ort.firewolf.domain.model.myself.participant.VillageCreatorSituation
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import org.springframework.stereotype.Service

@Service
class CreatorDomainService {

    fun convertToSituation(
        village: Village,
        player: Player?
    ): VillageCreatorSituation {
        return VillageCreatorSituation(
            isAvailableCreatorSetting = isAvailableCreatorSetting(village, player),
            isAvailableCreatorSay = isAvailableCreatorSay(village, player),
            isAvailableCancelVillage = isAvailableCancelVillage(village, player),
            isAvailableKick = isAvailableKick(village, player),
            isAvailableModifySetting = isAvailableModifySetting(village, player),
            isAvailableExtendEpilogue = isAvailableExtendEpilogue(village, player)
        )
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isAvailableCreatorSetting(
        village: Village,
        player: Player?
    ): Boolean {
        player ?: return false
        if (village.status.isFinished()) return false
        // 管理者か村建てならok
        if (village.creatorPlayerId == player.id) return true
        if (village.dummyChara().playerId == player.id) return true
        return false
    }

    private fun isAvailableCreatorSay(
        village: Village,
        player: Player?
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return true
    }

    private fun isAvailableCancelVillage(
        village: Village,
        player: Player?
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableKick(
        village: Village,
        player: Player?
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableModifySetting(
        village: Village,
        player: Player?
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableExtendEpilogue(village: Village, player: Player?): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isEpilogue() // エピローグ中のみ可能
    }
}