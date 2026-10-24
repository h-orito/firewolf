package com.ort.firewolf.domain.service.creator

import com.ort.firewolf.domain.model.myself.participant.VillageCreatorSituation
import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class CreatorDomainService {
    fun convertToSituation(
        village: Village,
        player: Player?,
    ): VillageCreatorSituation {
        return VillageCreatorSituation(
            availableCreatorSetting = isAvailableCreatorSetting(village, player),
            availableCreatorSay = isAvailableCreatorSay(village, player),
            availableCancelVillage = isAvailableCancelVillage(village, player),
            availableKick = isAvailableKick(village, player),
            availableModifySetting = isAvailableModifySetting(village, player),
            availableExtendEpilogue = isAvailableExtendEpilogue(village, player),
        )
    }

    // 村建ての操作は画面と同じくフェーズで制限する (管理者は運営のため制限しない)
    fun assertKick(
        village: Village,
        isAdmin: Boolean,
    ) {
        if (!isAdmin && !village.status.isPrologue()) throw FirewolfBusinessException("プロローグ中のみキックできます")
    }

    fun assertCancelVillage(
        village: Village,
        isAdmin: Boolean,
    ) {
        if (!isAdmin && !village.status.isPrologue()) throw FirewolfBusinessException("プロローグ中のみ廃村にできます")
    }

    fun assertExtendEpilogue(
        village: Village,
        isAdmin: Boolean,
    ) {
        if (!isAdmin && !village.status.isEpilogue()) throw FirewolfBusinessException("エピローグ中のみ延長できます")
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun isAvailableCreatorSetting(
        village: Village,
        player: Player?,
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
        player: Player?,
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return true
    }

    private fun isAvailableCancelVillage(
        village: Village,
        player: Player?,
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableKick(
        village: Village,
        player: Player?,
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableModifySetting(
        village: Village,
        player: Player?,
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isPrologue() // プロローグ中のみ可能
    }

    private fun isAvailableExtendEpilogue(
        village: Village,
        player: Player?,
    ): Boolean {
        if (!this.isAvailableCreatorSetting(village, player)) return false
        return village.status.isEpilogue() // エピローグ中のみ可能
    }
}
