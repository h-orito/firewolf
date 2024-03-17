package com.ort.firewolf.domain.service.message.system

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.service.message.MessageTypeDomainService
import org.springframework.stereotype.Service

/**
 * 共鳴の相互確認用メッセージ
 */
@Service
class SympathizerMessageDomainService : MessageTypeDomainService {

    override fun isViewable(
        village: Village,
        myself: VillageParticipant?,
        player: Player?,
        day: Int
    ): Boolean = village.isViewableSympathizerMessage() || myself?.isViewableSympathizerMessage() ?: false
}
