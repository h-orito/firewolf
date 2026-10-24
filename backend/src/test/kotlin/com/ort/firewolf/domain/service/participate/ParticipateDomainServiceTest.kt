package com.ort.firewolf.domain.service.participate

import com.ort.firewolf.domain.model.player.Player
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ParticipateDomainServiceTest {
    private val service = ParticipateDomainService()

    private fun player(id: Int): Player {
        val player = mock(Player::class.java)
        `when`(player.id).thenReturn(id)
        `when`(player.isAvailableParticipate(10)).thenReturn(true)
        return player
    }

    private fun village(
        alreadyParticipant: VillageParticipant? = null,
        isAvailableParticipate: Boolean = true,
        isAlreadyParticipateCharacter: Boolean = false,
    ): Village {
        val participants = mock(VillageParticipants::class.java)
        `when`(participants.findByPlayerId(1)).thenReturn(alreadyParticipant)
        val village = mock(Village::class.java)
        `when`(village.id).thenReturn(10)
        `when`(village.allParticipants()).thenReturn(participants)
        `when`(village.isAvailableParticipate()).thenReturn(isAvailableParticipate)
        `when`(village.isAvailableSpectate(20)).thenReturn(isAvailableParticipate)
        `when`(village.isAlreadyParticipateCharacter(5)).thenReturn(isAlreadyParticipateCharacter)
        return village
    }

    private fun assertCapacity(
        village: Village,
        isSpectate: Boolean = false,
    ) = service.assertParticipateCapacity(player(1), village, 5, isSpectate, 20)

    @Test
    @DisplayName("空きがあり、未参加で、キャラが重複していなければ参加できる")
    fun available() {
        assertDoesNotThrow { assertCapacity(village()) }
        assertDoesNotThrow { assertCapacity(village(), isSpectate = true) }
    }

    @Test
    @DisplayName("既に参加しているプレイヤーは参加できない")
    fun alreadyParticipating() {
        assertThrows<FirewolfBusinessException> { assertCapacity(village(alreadyParticipant = mock(VillageParticipant::class.java))) }
    }

    @Test
    @DisplayName("定員に達していれば参加・見学できない")
    fun full() {
        assertThrows<FirewolfBusinessException> { assertCapacity(village(isAvailableParticipate = false)) }
        assertThrows<FirewolfBusinessException> { assertCapacity(village(isAvailableParticipate = false), isSpectate = true) }
    }

    @Test
    @DisplayName("既に使われているキャラでは参加できない")
    fun duplicatedCharacter() {
        assertThrows<FirewolfBusinessException> { assertCapacity(village(isAlreadyParticipateCharacter = true)) }
    }
}
