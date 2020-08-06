package com.ort.firewolf.domain.service.daychange

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.FirewolfTest
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.village.VillageDays
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import com.ort.firewolf.domain.model.village.setting.PersonCapacity
import com.ort.firewolf.dummy.DummyDomainModelCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
class PrologueDomainServiceTest : FirewolfTest() {

    @Autowired
    lateinit var prologueDomainService: PrologueDomainService

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_addDayIfNeeded_開始時刻になっていない() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().plusSeconds(1L)
                        )
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = prologueDomainService.addDayIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_extendIfNeeded_人数不足() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().minusSeconds(1L)
                        )
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = prologueDomainService.extendIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.village.setting.time.startDatetime).isAfter(dayChange.village.setting.time.startDatetime)
        assertThat(afterDayChange.village.day.latestDay().dayChangeDatetime).isAfter(dayChange.village.day.latestDay().dayChangeDatetime)
    }

    @Test
    fun test_addDayIfNeeded_人数が足りている() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().minusSeconds(1L)
                        )
                    )
                ),
                setting = DummyDomainModelCreator.createDummyVillageSettings().copy(
                    capacity = PersonCapacity(2, 10)
                ),
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        DummyDomainModelCreator.createDummyVillageParticipant(),
                        DummyDomainModelCreator.createDummyVillageParticipant()
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = prologueDomainService.addDayIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.village.day.dayList.size).isEqualTo(2)
    }

    @Test
    fun test_dayChange() {
        // ## Arrange ##
        val dummyParticipant = DummyDomainModelCreator.createDummyVillageParticipant()
        val villagerList = (1..10).toList().map { DummyDomainModelCreator.createDummyVillageParticipant() }
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay(),
                        DummyDomainModelCreator.createDummyVillageDay()
                    )
                ),
                setting = DummyDomainModelCreator.createDummyVillageSettings().copy(
                    capacity = PersonCapacity(10, 16),
                    charachip = DummyDomainModelCreator.createDummyVillageCharachip().copy(
                        dummyCharaId = dummyParticipant.charaId
                    )
                ),
                participant = VillageParticipants(
                    count = villagerList.size + 2,
                    memberList = villagerList + dummyParticipant
                )
            )
        )
        val charas = Charas(
            villagerList.map { DummyDomainModelCreator.createDummyChara().copy(id = it.charaId) }
                + DummyDomainModelCreator.createDummyChara().copy(id = dummyParticipant.charaId)
        )

        // ## Act ##
        val afterDayChange = prologueDomainService.dayChange(dayChange, charas)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.messages.list.size).isEqualTo(4)
        afterDayChange.messages.list.forEach { println(it.content.text) }
        assertThat(afterDayChange.village.participant.memberList.all { it.skill != null }).`as`("役職が割り振られている").isTrue()
        assertThat(afterDayChange.village.status.toCdef()).isEqualTo(CDef.VillageStatus.進行中)
    }
}