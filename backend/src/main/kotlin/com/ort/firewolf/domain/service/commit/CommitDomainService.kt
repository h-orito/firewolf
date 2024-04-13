package com.ort.firewolf.domain.service.commit

import com.ort.firewolf.domain.model.commit.Commit
import com.ort.firewolf.domain.model.message.Message
import com.ort.firewolf.domain.model.myself.participant.VillageCommitSituation
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.springframework.stereotype.Service

@Service
class CommitDomainService {

    fun convertToSituation(
        village: Village,
        participant: VillageParticipant?,
        commit: Commit?
    ): VillageCommitSituation {
        return VillageCommitSituation(
            availableCommit = isAvailableCommit(village, participant),
            committing = commit != null
        )
    }

    /**
     * @param village village
     * @param participant 参加者
     * @return コミットできるか
     */
    fun isAvailableCommit(
        village: Village,
        participant: VillageParticipant?
    ): Boolean {
        // 村として可能か
        if (!village.isAvailableCommit()) return false
        // 参加者として可能か
        participant ?: return false
        return participant.isAvailableCommit(village.dummyChara().id)
    }

    /**
     * コミットチェック
     * @param village village
     * @param participant 参加者
     */
    fun assertCommit(
        village: Village,
        participant: VillageParticipant?
    ) {
        if (!isAvailableCommit(village, participant)) throw FirewolfBusinessException("コミットできません")
    }

    /**
     * コミットメッセージ
     */
    fun createCommitMessage(myself: VillageParticipant, doCommit: Boolean, villageDayId: Int): Message =
        Message.createPrivateSystemMessage(getCommitSetMessage(doCommit, myself), villageDayId)

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun getCommitSetMessage(doCommit: Boolean, myself: VillageParticipant): String =
        if (doCommit) "${myself.name()}が時短希望しました。" else "${myself.name()}が時短希望を取り消しました。"
}