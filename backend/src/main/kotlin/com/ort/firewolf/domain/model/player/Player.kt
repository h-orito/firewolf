package com.ort.firewolf.domain.model.player

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import com.ort.firewolf.fw.security.FirewolfUser

data class Player(
    val id: Int,
    val nickname: String,
    val twitterUserName: String?,
    val otherSiteName: String?,
    val introduction: String?,
    val isRestrictedParticipation: Boolean,
    val shouldCheckAccessInfo: Boolean,
    val participateProgressVillageIdList: List<Int> = listOf(),
    val participateFinishedVillageIdList: List<Int> = listOf(),
    val createProgressVillageIdList: List<Int> = listOf(),
    val createFinishedVillageIdList: List<Int> = listOf()
) {
    fun restrictParticipation(): Player {
        return this.copy(isRestrictedParticipation = true)
    }

    /**
     * 村作成チェック
     * @param user user
     */
    fun assertCreateVillage(user: FirewolfUser?) {
        if (!isAvailableCreateVillage(user)) throw FirewolfBusinessException("村を作成できません")
    }

    /**
     * @return 村に参戦可能か
     */
    fun isAvailableParticipate(): Boolean {
        if (isParticipatingProgressVillage()) return false
        if (isRestrictedParticipation) return false
        return true
    }

    /**
     * @param user user
     * @return 村を作成可能か
     */
    fun isAvailableCreateVillage(user: FirewolfUser?): Boolean {
        user ?: return false
        if (user.authority == CDef.Authority.管理者) return true
        if (isParticipatingProgressVillage()) return false
        if (isRestrictedParticipation) return false
        if (isProgressCreateVillage()) return false
        return true
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // 進行中の村に参加しているか
    private fun isParticipatingProgressVillage(): Boolean = participateProgressVillageIdList.isNotEmpty()

    // 作成した村の勝敗が決していないか
    private fun isProgressCreateVillage(): Boolean = createProgressVillageIdList.isNotEmpty()

    fun existsDifference(player: Player): Boolean {
        return id != player.id || isRestrictedParticipation != player.isRestrictedParticipation
    }
}