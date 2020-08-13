package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.FirewolfTest
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.domain.model.dead.Dead
import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.domain.model.village.VillageDays
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import com.ort.firewolf.dummy.DummyDomainModelCreator
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class AutopsyDomainServiceTest : FirewolfTest() {

    @Autowired
    lateinit var autopsyDomainService: AutopsyDomainService

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_process_検死官がいない() {
        // ## Arrange ##
        val deadCoroner = DummyDomainModelCreator.createDummyDeadPsychic().copy(skill = Skill(CDef.Skill.検死官))
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(deadCoroner)
                )
            )
        )
        val charas = DummyDomainModelCreator.createDummyCharas()

        // ## Act ##
        val afterDayChange = autopsyDomainService.addAutopsyMessage(dayChange, charas)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_process_検死官がいるが検死できる死者がいない() {
        // ## Arrange ##
        val aliveCoroner = DummyDomainModelCreator.createDummyAlivePsychic().copy(skill = Skill(CDef.Skill.検死官))
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        aliveCoroner,
                        DummyDomainModelCreator.createDummyDeadVillager().copy(
                            dead = Dead(CDef.DeadReason.処刑, latestDay)
                        )
                    )
                ),
                day = VillageDays(listOf(latestDay))
            )
        )
        val charas = DummyDomainModelCreator.createDummyCharas()

        // ## Act ##
        val afterDayChange = autopsyDomainService.addAutopsyMessage(dayChange, charas)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_process_検死あり() {
        // ## Arrange ##
        val aliveCoroner = DummyDomainModelCreator.createDummyAlivePsychic().copy(skill = Skill(CDef.Skill.検死官))
        val yesterday = DummyDomainModelCreator.createDummyFirstVillageDay()
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val deadFox = DummyDomainModelCreator.createDummyDeadWolf().copy(
            skill = Skill(CDef.Skill.妖狐),
            dead = Dead(CDef.DeadReason.呪殺, latestDay)
        )
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        aliveCoroner,
                        deadFox
                    )
                ),
                day = VillageDays(listOf(yesterday, latestDay))
            )
        )
        val charas = Charas(
            listOf(
                DummyDomainModelCreator.createDummyChara().copy(id = aliveCoroner.charaId),
                DummyDomainModelCreator.createDummyChara().copy(id = deadFox.charaId)
            )
        )

        // ## Act ##
        val afterDayChange = autopsyDomainService.addAutopsyMessage(dayChange, charas)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isTrue()
        Assertions.assertThat(afterDayChange.messages.list).isNotEmpty
        Assertions.assertThat(afterDayChange.messages.list.first()).satisfies { message ->
            Assertions.assertThat(message.content.type.toCdef()).isEqualTo(CDef.MessageType.検死結果)
            Assertions.assertThat(message.content.text).contains("呪殺死のようだ")
        }
    }
}