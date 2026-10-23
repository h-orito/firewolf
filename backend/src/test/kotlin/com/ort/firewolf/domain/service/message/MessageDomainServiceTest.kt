package com.ort.firewolf.domain.service.message

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`

class MessageDomainServiceTest {
    private val village: Village = mock(Village::class.java, RETURNS_DEEP_STUBS)
    private val sender: VillageParticipant = mock(VillageParticipant::class.java)
    private val service: MessageDomainService =
        spy(
            MessageDomainService(
                mock(com.ort.firewolf.domain.service.message.say.SayDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.PsychicMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.GuruPsychicMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.AttackMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.AutopsyMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.MasonMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.SympathizerMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.FoxMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.FanaticMessageDomainService::class.java),
                mock(com.ort.firewolf.domain.service.message.system.PrivateSystemMessageDomainService::class.java),
            ),
        )

    init {
        `when`(sender.id).thenReturn(1)
        `when`(village.day.latestDay().day).thenReturn(2)
    }

    private fun viewable(
        participant: VillageParticipant?,
        messageType: CDef.MessageType,
        isViewable: Boolean,
    ) {
        doReturn(isViewable).`when`(service).isViewableMessage(village, participant, null, messageType, 2)
    }

    @Test
    @DisplayName("見られない種別の他人の発言 (囁きなど) は記録しない")
    fun notViewableOthersMessage() {
        viewable(sender, CDef.MessageType.人狼の囁き, false)
        assertFalse(service.isAnchorableMessage(village, sender, CDef.MessageType.人狼の囁き, 2))
    }

    @Test
    @DisplayName("見られる種別の他人の発言は記録する")
    fun viewableOthersMessage() {
        viewable(sender, CDef.MessageType.人狼の囁き, true)
        assertTrue(service.isAnchorableMessage(village, sender, CDef.MessageType.人狼の囁き, 2))
    }

    @Test
    @DisplayName("自分の発言 (独り言など) は種別を見られなくても記録する")
    fun ownMessage() {
        viewable(sender, CDef.MessageType.独り言, false)
        assertTrue(service.isAnchorableMessage(village, sender, CDef.MessageType.独り言, 1))
    }

    @Test
    @DisplayName("発言者のいない発言 (システム発言) は、誰でも見られる種別だけ記録する")
    fun noSender() {
        viewable(null, CDef.MessageType.通常発言, true)
        viewable(null, CDef.MessageType.人狼の囁き, false)
        assertTrue(service.isAnchorableMessage(village, null, CDef.MessageType.通常発言, 2))
        assertFalse(service.isAnchorableMessage(village, null, CDef.MessageType.人狼の囁き, 2))
    }
}
