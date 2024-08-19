package com.ort.firewolf.domain.model.village.participant

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.camp.Camp
import com.ort.firewolf.domain.model.camp.toModel
import com.ort.firewolf.domain.model.dead.Dead
import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.skill.SkillRequest
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageDay
import com.ort.firewolf.domain.model.village.participant.coming_out.ComingOuts

data class VillageParticipant(
    val id: Int,
    val charaName: VillageParticipantName,
    val charaId: Int,
    val playerId: Int?,
    val status: VillageParticipantStatus,
    val dead: Dead?,
    val isSpectator: Boolean,
    val isGone: Boolean,
    val skill: Skill?,
    val skillRequest: SkillRequest,
    val isWin: Boolean?,
    val camp: Camp?,
    val commigOuts: ComingOuts,
    val ipAddresses: List<String>,
    val notification: VillageParticipantNotificationCondition?
) {
    // ===================================================================================
    //                                                                                read
    //                                                                           =========
    fun name(): String = "[${shortName()}] ${charaName.name}"
    fun shortName(): String = charaName.shortName
    fun isAdmin(): Boolean = playerId == 1
    fun isAlive(): Boolean = dead == null
    fun isDead(): Boolean = !isAlive()
    fun isAvailableSkillRequest(): Boolean = !isSpectator
    fun isAvailableCommit(dummyParticipantId: Int): Boolean = !isSpectator && isAlive() && id != dummyParticipantId
    fun isAvailableComingOut(): Boolean = !isSpectator && isAlive()
    fun isAvailableSay(isEpilogue: Boolean): Boolean = !isDead() || !dead!!.isSuddenly() || isEpilogue
    fun isSayableNormalSay(isEpilogue: Boolean): Boolean = isAdmin() || (!isSpectator && (isAlive() || isEpilogue))
    fun isViewableWerewolfSay(): Boolean = isAdmin() || skill?.isViewableWerewolfSay() ?: false
    fun isSayableWerewolfSay(): Boolean = isAdmin() || (isAlive() && skill?.isAvailableWerewolfSay() ?: false)
    fun isViewableSympathizeSay(): Boolean = isAdmin() || skill?.isViewableSympathizeSay() ?: false
    fun isSayableSympathizeSay(): Boolean = isAdmin() || (isAlive() && skill?.isAvailableSympathizeSay() ?: false)
    fun isViewableLoversSay(): Boolean = isAdmin() || status.hasLover()
    fun isSayableLoversSay(): Boolean = isAdmin() || (isAlive() && status.hasLover())
    fun isViewableGraveSay(): Boolean = isAdmin() || isSpectator || (isDead() && !dead!!.isSuddenly())
    fun isSayableGraveSay(): Boolean = isAdmin() || (!isSpectator && isDead() && !dead!!.isSuddenly())
    fun isViewableMonologueSay(): Boolean = isAdmin()
    fun isSayableMonologueSay(): Boolean = true
    fun isViewableSpectateSay(): Boolean = isAdmin() || isSpectator || (isDead() && !dead!!.isSuddenly())
    fun isSayableSpectateSay(): Boolean = isAdmin() || isSpectator
    fun isViewableAttackMessage(): Boolean = skill?.isAvailableWerewolfSay() ?: false
    fun isViewableAutopsyMessage(): Boolean = skill?.hasAutopsyAbility() ?: false
    fun isViewableFanaticMessage(): Boolean = skill?.canRecognizeWolf() ?: false
    fun isViewableMasonMessage(): Boolean = skill?.canRecognizeEachMason() ?: false
    fun isViewableSympathizerMessage(): Boolean = skill?.canRecognizeEachSympathizer() ?: false
    fun isViewableFoxMessage(): Boolean = skill?.canRecognizeFoxs() ?: false
    fun isViewablePsychicMessage(): Boolean = skill?.hasPsychicAbility() ?: false
    fun isViewableGuruPsychicMessage(): Boolean = skill?.hasGuruPsychicAbility() ?: false
    fun isViewableSecretSay(): Boolean = isAdmin()
    fun isSayableSecretSay(): Boolean = true
    fun isViewablePrivateSystemMessage(): Boolean = isAdmin()

    // 能力行使可能か
    fun canUseAbility(): Boolean = !isSpectator

    // 投票可能か
    fun isAvailableVote(): Boolean = isAlive() && !isSpectator

    // 名前変更可能か
    fun canChangeName(isEpilogue: Boolean): Boolean = dead == null || !dead.isSuddenly() || isEpilogue

    fun getTargetLovers(village: Village): VillageParticipants {
        val list = village.participant.memberList.filter { status.loverIdList.contains(it.id) }
        return VillageParticipants(count = list.size, memberList = list)
    }

    // 差分有無
    fun existsDifference(participant: VillageParticipant): Boolean {
        if (id != participant.id) return true
        if (charaName.existsDifference(participant.charaName)) return true
        if (charaId != participant.charaId) return true
        if (playerId != participant.playerId) return true
        if (!status.isSame(participant.status)) return true
        if (dead?.code != participant.dead?.code) return true
        if (isSpectator != participant.isSpectator) return true
        if (isGone != participant.isGone) return true
        if (skill?.code != participant.skill?.code) return true
        if (skillRequest.first.code != participant.skillRequest.first.code) return true
        if (skillRequest.second.code != participant.skillRequest.second.code) return true
        if (ipAddresses.size != participant.ipAddresses.size) return true
        if (camp?.code != participant.camp?.code) return true
        if (isWin != participant.isWin) return true
        return false
    }

    // ===================================================================================
    //                                                                              update
    //                                                                           =========
    // 退村
    fun gone(): VillageParticipant = this.copy(isGone = true)

    // 突然死
    fun suddenlyDeath(villageDay: VillageDay): VillageParticipant =
        this.copy(dead = Dead(CDef.DeadReason.突然, villageDay))

    // 処刑
    fun execute(villageDay: VillageDay): VillageParticipant = this.copy(dead = Dead(CDef.DeadReason.処刑, villageDay))

    // 襲撃
    fun attack(villageDay: VillageDay): VillageParticipant = this.copy(dead = Dead(CDef.DeadReason.襲撃, villageDay))

    // 呪殺
    fun divineKill(villageDay: VillageDay): VillageParticipant =
        this.copy(dead = Dead(CDef.DeadReason.呪殺, villageDay))

    // 後追い
    fun suicide(villageDay: VillageDay): VillageParticipant = this.copy(dead = Dead(CDef.DeadReason.後追, villageDay))

    // 求愛
    fun court(participantId: Int): VillageParticipant = love(participantId)

    // 求愛された
    fun courted(participantId: Int): VillageParticipant = love(participantId)

    // 役職割り当て
    fun assignSkill(skill: Skill): VillageParticipant =
        this.copy(
            skill = skill,
            camp = getCurrentWinCamp(status, skill)
        )

    // 希望役職
    fun changeSkillRequest(first: CDef.Skill, second: CDef.Skill): VillageParticipant =
        this.copy(skillRequest = SkillRequest(Skill(first), Skill(second)))

    // 名前変更
    fun changeName(name: String, shortName: String): VillageParticipant =
        this.copy(charaName = VillageParticipantName(name = name, shortName = shortName))

    // 勝敗
    fun judgeWin(winCamp: Camp): VillageParticipant = copy(
        isWin = when {
            // 突然死したことがあったら必ず敗北
            dead?.isSuddenly() == true -> false
            // 勝敗判定陣営が一致していたら勝利
            else -> winCamp.toCdef() == camp?.toCdef()
        }
    )

    // IPアドレス追加
    fun addIpAddress(ipAddress: String): VillageParticipant =
        this.copy(ipAddresses = (ipAddresses + ipAddress).distinct())

    private fun love(participantId: Int): VillageParticipant = copy(
        status = status.addLover(participantId),
        camp = CDef.Camp.恋人陣営.toModel()
    )

    private fun getCurrentWinCamp(currentStatus: VillageParticipantStatus, currentSkill: Skill): Camp =
        when {
            currentStatus.hasLover() -> CDef.Camp.恋人陣営.toModel()
            else -> currentSkill.camp()
        }
}