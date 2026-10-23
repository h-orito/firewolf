package com.ort.firewolf.domain.service.creator

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageStatus
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CreatorDomainServiceTest {
    private val service = CreatorDomainService()

    private fun village(status: CDef.VillageStatus): Village {
        val village = mock(Village::class.java)
        `when`(village.status).thenReturn(VillageStatus(status))
        return village
    }

    @ParameterizedTest
    @EnumSource(CDef.VillageStatus::class)
    fun kickAndCancelOnlyInPrologue(status: CDef.VillageStatus) {
        val village = village(status)
        if (status == CDef.VillageStatus.プロローグ) {
            assertDoesNotThrow { service.assertKick(village, false) }
            assertDoesNotThrow { service.assertCancelVillage(village, false) }
        } else {
            assertThrows<FirewolfBusinessException> { service.assertKick(village, false) }
            assertThrows<FirewolfBusinessException> { service.assertCancelVillage(village, false) }
        }
        // 管理者はフェーズによらず使える
        assertDoesNotThrow { service.assertKick(village, true) }
        assertDoesNotThrow { service.assertCancelVillage(village, true) }
    }

    @ParameterizedTest
    @EnumSource(CDef.VillageStatus::class)
    fun extendEpilogueOnlyInEpilogue(status: CDef.VillageStatus) {
        val village = village(status)
        if (status == CDef.VillageStatus.エピローグ) {
            assertDoesNotThrow { service.assertExtendEpilogue(village, false) }
        } else {
            assertThrows<FirewolfBusinessException> { service.assertExtendEpilogue(village, false) }
        }
        assertDoesNotThrow { service.assertExtendEpilogue(village, true) }
    }
}
