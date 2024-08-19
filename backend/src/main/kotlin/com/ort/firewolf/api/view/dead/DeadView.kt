package com.ort.firewolf.api.view.dead

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.dead.Dead
import com.ort.firewolf.domain.model.village.VillageDay

data class DeadView(
    val code: String,
    val reason: String,
    val villageDay: VillageDay
) {

    constructor(
        dead: Dead,
        shouldHidePlayer: Boolean
    ) : this(
        code = if (shouldHidePlayer) convertToHideCode(dead) else dead.code,
        reason = if (shouldHidePlayer) convertToHideReason(dead) else dead.reason,
        villageDay = dead.villageDay
    )

    companion object {
        private fun convertToHideCode(dead: Dead): String {
            // 処刑、突然、後追いはそのまま出してok
            if (dead.code == CDef.DeadReason.処刑.code() ||
                dead.code == CDef.DeadReason.突然.code() ||
                dead.code == CDef.DeadReason.後追.code()
            ) {
                return dead.code
            }
            // それ以外は無惨
            return "MISERABLE"
        }

        private fun convertToHideReason(dead: Dead): String {
            // 処刑、突然、後追はそのまま出してok
            if (dead.code == CDef.DeadReason.処刑.code() ||
                dead.code == CDef.DeadReason.突然.code() ||
                dead.code == CDef.DeadReason.後追.code()
            ) {
                return dead.reason
            }
            // それ以外は無惨
            return "無惨"
        }
    }
}