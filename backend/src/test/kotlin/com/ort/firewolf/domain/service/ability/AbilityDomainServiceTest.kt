package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AbilityDomainServiceTest {
    private val village: Village = mock(Village::class.java)
    private val attackDomainService: AttackDomainService = mock(AttackDomainService::class.java)
    private val service =
        AbilityDomainService(
            attackDomainService,
            mock(DivineDomainService::class.java),
            mock(WiseDivineDomainService::class.java),
            mock(GuardDomainService::class.java),
            mock(WandererGuardDomainService::class.java),
            mock(CourtDomainService::class.java),
            mock(HiyashichukaDomainService::class.java),
            mock(EmotionDomainService::class.java),
        )

    private fun participant(id: Int): VillageParticipant {
        val p = mock(VillageParticipant::class.java)
        `when`(p.id).thenReturn(id)
        return p
    }

    // 生存している人狼: 1, 2 / 本人以外の参加者: 3
    private val wolf1 = participant(1)
    private val wolf2 = participant(2)
    private val attack = AbilityType(CDef.AbilityType.襲撃)
    private val divine = AbilityType(CDef.AbilityType.占い)

    init {
        `when`(attackDomainService.getSelectableAttacker(village)).thenReturn(listOf(wolf1, wolf2))
    }

    @Test
    @DisplayName("襲撃: 生存している人狼なら他の人狼を襲撃者にできる")
    fun attackByAnotherAliveWolf() {
        assertDoesNotThrow { service.assertAbilityActor(village, wolf1, 2, attack) }
    }

    @Test
    @DisplayName("襲撃: 人狼でない参加者を襲撃者にできない")
    fun attackByNonWolf() {
        assertThrows<FirewolfBusinessException> { service.assertAbilityActor(village, wolf1, 3, attack) }
    }

    @Test
    @DisplayName("襲撃: 襲撃者の指定が必要")
    fun attackWithoutActor() {
        assertThrows<FirewolfBusinessException> { service.assertAbilityActor(village, wolf1, null, attack) }
    }

    @Test
    @DisplayName("襲撃以外: 行使者を指定しなければ本人")
    fun divineWithoutActor() {
        assertDoesNotThrow { service.assertAbilityActor(village, participant(5), null, divine) }
    }

    @Test
    @DisplayName("襲撃以外: 本人の ID なら指定できる")
    fun divineByMyself() {
        assertDoesNotThrow { service.assertAbilityActor(village, participant(5), 5, divine) }
    }

    @Test
    @DisplayName("襲撃以外: 他人の ID は指定できない")
    fun divineByAnother() {
        assertThrows<FirewolfBusinessException> { service.assertAbilityActor(village, participant(5), 3, divine) }
    }
}
