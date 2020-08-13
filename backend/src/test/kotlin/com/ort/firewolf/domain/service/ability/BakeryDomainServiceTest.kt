package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.FirewolfTest
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
class BakeryDomainServiceTest : FirewolfTest() {

    @Autowired
    lateinit var bakeryDomainService: BakeryDomainService

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_パン屋がいない() {
        // ## Arrange ##
        val deadPsychic = DummyDomainModelCreator.createDummyDeadPsychic()
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(deadPsychic)
                ),
                day = VillageDays(listOf(latestDay))
            )
        )

        // ## Act ##
        val afterDayChange = bakeryDomainService.addBakeryMessage(dayChange)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_パン屋が生存() {
        // ## Arrange ##
        val aliveBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(skill = Skill(CDef.Skill.パン屋))
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(aliveBakery)
                ),
                day = VillageDays(listOf(latestDay))
            )
        )

        // ## Act ##
        val afterDayChange = bakeryDomainService.addBakeryMessage(dayChange)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isTrue()
        Assertions.assertThat(afterDayChange.messages.list.first().content.text).contains("焼いてくれた")
    }

    @Test
    fun test_パン屋が1名死亡1名生存() {
        // ## Arrange ##
        val aliveBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(skill = Skill(CDef.Skill.パン屋))
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val deadBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(
            skill = Skill(CDef.Skill.パン屋),
            dead = Dead(CDef.DeadReason.処刑, latestDay)
        )
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(aliveBakery, deadBakery)
                ),
                day = VillageDays(listOf(latestDay))
            )
        )

        // ## Act ##
        val afterDayChange = bakeryDomainService.addBakeryMessage(dayChange)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isTrue()
        Assertions.assertThat(afterDayChange.messages.list.first().content.text).contains("焼いてくれた")
    }

    @Test
    fun test_パン屋が全滅した日() {
        // ## Arrange ##
        val yesterday = DummyDomainModelCreator.createDummyFirstVillageDay()
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val yesterdayDeadBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(
            skill = Skill(CDef.Skill.パン屋),
            dead = Dead(CDef.DeadReason.処刑, yesterday)
        )
        val deadBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(
            skill = Skill(CDef.Skill.パン屋),
            dead = Dead(CDef.DeadReason.処刑, latestDay)
        )
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(yesterdayDeadBakery, deadBakery)
                ),
                day = VillageDays(listOf(yesterday, latestDay))
            )
        )

        // ## Act ##
        val afterDayChange = bakeryDomainService.addBakeryMessage(dayChange)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isTrue()
        Assertions.assertThat(afterDayChange.messages.list.first().content.text).contains("今日からは")
    }

    @Test
    fun test_パン屋が全滅した翌日() {
        // ## Arrange ##
        val twoDaysAgo = DummyDomainModelCreator.createDummyFirstVillageDay()
        val yesterday = DummyDomainModelCreator.createDummyFirstVillageDay()
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val twoDaysAgoDeadBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(
            skill = Skill(CDef.Skill.パン屋),
            dead = Dead(CDef.DeadReason.処刑, twoDaysAgo)
        )
        val yesterdayDeadBakery = DummyDomainModelCreator.createDummyAliveVillager().copy(
            skill = Skill(CDef.Skill.パン屋),
            dead = Dead(CDef.DeadReason.処刑, yesterday)
        )
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 1,
                    memberList = listOf(twoDaysAgoDeadBakery, yesterdayDeadBakery)
                ),
                day = VillageDays(listOf(twoDaysAgo, yesterday, latestDay))
            )
        )

        // ## Act ##
        val afterDayChange = bakeryDomainService.addBakeryMessage(dayChange)

        // ## Assert ##
        Assertions.assertThat(afterDayChange.isChange).isFalse()
    }
}