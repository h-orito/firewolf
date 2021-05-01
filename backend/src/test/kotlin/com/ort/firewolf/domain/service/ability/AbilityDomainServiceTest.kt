package com.ort.firewolf.domain.service.ability

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.FirewolfTest
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.village.VillageDays
import com.ort.firewolf.domain.model.village.VillageStatus
import com.ort.firewolf.domain.model.village.ability.VillageAbilities
import com.ort.firewolf.domain.model.village.ability.VillageAbility
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import com.ort.firewolf.dummy.DummyDomainModelCreator
import com.ort.firewolf.fw.exception.FirewolfBadRequestException
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class AbilityDomainServiceTest : FirewolfTest() {

    @Autowired
    lateinit var abilityDomainService: AbilityDomainService

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_getSelectableTargetList_未参加者() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage()
        val participant = null
        val abilityType = AbilityType(CDef.AbilityType.占い)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        val selectableTargetList = abilityDomainService.getSelectableTargetList(
            village,
            participant,
            villageAbilities,
            abilityType
        )

        // ## Assert ##
        assertThat(selectableTargetList).`as`("参加していないので選択不可能").isEmpty()
    }

    @Test
    fun test_getSelectableTargetList_終了した村() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage().changeStatus(CDef.VillageStatus.終了)
        val participant = DummyDomainModelCreator.createDummyVillageParticipant()
        val abilityType = AbilityType(CDef.AbilityType.占い)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        val selectableTargetList = abilityDomainService.getSelectableTargetList(
            village,
            participant,
            villageAbilities,
            abilityType
        )

        // ## Assert ##
        assertThat(selectableTargetList).`as`("村が終了しているので選択不可能").isEmpty()
    }

    @Test
    fun test_getSelectableTargetList_対象あり() {
        // ## Arrange ##
        val participant = DummyDomainModelCreator.createDummyVillageParticipant().copy(
            id = 1
        )
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                3,
                listOf(
                    participant.copy(id = 1),
                    participant.copy(id = 2),
                    participant.copy(id = 3)
                )
            )
        )
        val abilityType = AbilityType(CDef.AbilityType.占い)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        val selectableTargetList = abilityDomainService.getSelectableTargetList(village, participant, villageAbilities, abilityType)

        // ## Assert ##
        assertThat(selectableTargetList.size).isEqualTo(2)
    }

    @Test
    fun test_getSelectingTarget_未参加者() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage()
        val participant = null
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()
        val abilityType = AbilityType(CDef.AbilityType.占い)

        // ## Act ##
        val target = abilityDomainService.getSelectingTarget(village, participant, villageAbilities, abilityType)

        // ## Assert ##
        assertThat(target).`as`("参加していないのでなし").isNull()
    }

    @Test
    fun test_getSelectingTarget_終了した村() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage().changeStatus(CDef.VillageStatus.終了)
        val participant = DummyDomainModelCreator.createDummyVillageParticipant()
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()
        val abilityType = AbilityType(CDef.AbilityType.占い)

        // ## Act ##
        val target = abilityDomainService.getSelectingTarget(village, participant, villageAbilities, abilityType)

        // ## Assert ##
        assertThat(target).`as`("村が終了しているのでなし").isNull()
    }

    @Test
    fun test_getSelectingTarget_対象あり() {
        // ## Arrange ##
        val participant = DummyDomainModelCreator.createDummyVillageParticipant().copy(id = 100)
        val targetParticipant = DummyDomainModelCreator.createDummyVillageParticipant().copy(id = 101)
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                count = 2,
                memberList = listOf(
                    participant,
                    targetParticipant
                )
            ),
            day = VillageDays(
                listOf(
                    DummyDomainModelCreator.createDummyVillageDay().copy(id = 2)
                )
            )
        )
        val villageAbilities = VillageAbilities(
            listOf(
                VillageAbility(
                    villageDayId = 2,
                    myselfId = participant.id,
                    targetId = targetParticipant.id,
                    abilityType = AbilityType(CDef.AbilityType.占い)
                )
            )
        )
        val abilityType = AbilityType(CDef.AbilityType.占い)

        // ## Act ##
        val target = abilityDomainService.getSelectingTarget(village, participant, villageAbilities, abilityType)

        // ## Assert ##
        assertThat(target?.id).`as`("").isEqualTo(targetParticipant.id)
    }

    @Test
    fun test_createAbilitySetMessage() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            day = VillageDays(
                listOf(
                    DummyDomainModelCreator.createDummyVillageDay()
                )
            )
        )
        val chara = DummyDomainModelCreator.createDummyChara()
        val targetChara = DummyDomainModelCreator.createDummyChara()
        val abilityType = AbilityType(CDef.AbilityType.占い)

        // ## Act ##
        val message = abilityDomainService.createAbilitySetMessage(village, chara, targetChara, abilityType)

        // ## Assert ##
        assertThat(message.content.text).isNotEmpty()
    }

    @Test(expected = FirewolfBadRequestException::class)
    fun test_assertAbility_その能力を持っていない() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage()
        val participant = DummyDomainModelCreator.createDummyAliveSeer()
        val targetId = null
        val abilityType = AbilityType(CDef.AbilityType.襲撃)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        // ## Assert ##
        abilityDomainService.assertAbility(village, participant, targetId, abilityType, villageAbilities)
    }

    @Test(expected = FirewolfBusinessException::class)
    fun test_assertAbility_対象なしにできない能力なのに対象なし() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage()
        val participant = DummyDomainModelCreator.createDummyAliveSeer()
        val targetId = null
        val abilityType = AbilityType(CDef.AbilityType.占い)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        // ## Assert ##
        abilityDomainService.assertAbility(village, participant, targetId, abilityType, villageAbilities)
    }

    fun test_assertAbility_対象なしにできる能力で対象なし() {
        // ## Arrange ##
        val village = DummyDomainModelCreator.createDummyVillage()
        val participant = DummyDomainModelCreator.createDummyAliveWolf()
        val targetId = null
        val abilityType = AbilityType(CDef.AbilityType.襲撃)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        // ## Assert ##
        abilityDomainService.assertAbility(village, participant, targetId, abilityType, villageAbilities)
    }

    @Test(expected = FirewolfBusinessException::class)
    fun test_assertAbility_対象あり_対象に選べない人を選ぼうとしている() {
        // ## Arrange ##
        val availableTarget = DummyDomainModelCreator.createDummyAliveHunter()
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                count = 1,
                memberList = listOf(availableTarget)
            ),
            day = VillageDays(listOf(DummyDomainModelCreator.createDummyVillageDay()))
        )
        val participant = DummyDomainModelCreator.createDummyAliveWolf()
        val targetId = 10001
        val abilityType = AbilityType(CDef.AbilityType.襲撃)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        // ## Assert ##
        abilityDomainService.assertAbility(village, participant, targetId, abilityType, villageAbilities)
    }

    @Test
    fun test_assertAbility_対象あり_正しい対象() {
        // ## Arrange ##
        val availableTarget = DummyDomainModelCreator.createDummyAliveHunter()
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                count = 1,
                memberList = listOf(availableTarget)
            ),
            day = VillageDays(listOf(DummyDomainModelCreator.createDummyVillageDay()))
        )
        val participant = DummyDomainModelCreator.createDummyAliveWolf()
        val targetId = availableTarget.id
        val abilityType = AbilityType(CDef.AbilityType.襲撃)
        val villageAbilities = DummyDomainModelCreator.createDummyVillageAbilities()

        // ## Act ##
        // ## Assert ##
        abilityDomainService.assertAbility(village, participant, targetId, abilityType, villageAbilities)
    }

    @Test
    fun test_isUsable_行使できる() {
        // ## Arrange ##
        val participant = DummyDomainModelCreator.createDummyAliveWolf()
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                count = 1,
                memberList = listOf(participant)
            ),
            day = VillageDays(listOf(DummyDomainModelCreator.createDummyVillageDay()))
        )
        val abilityType = AbilityType(CDef.AbilityType.襲撃)

        // ## Act ##
        // ## Assert ##
        assertThat(abilityDomainService.isUsable(village, participant, abilityType)).isTrue()
    }

    @Test
    fun test_isUsable_行使できない() {
        // ## Arrange ##
        val participant = DummyDomainModelCreator.createDummyDeadWolf()
        val village = DummyDomainModelCreator.createDummyVillage().copy(
            status = VillageStatus(CDef.VillageStatus.進行中),
            participant = VillageParticipants(
                count = 1,
                memberList = listOf(participant)
            ),
            day = VillageDays(listOf(DummyDomainModelCreator.createDummyVillageDay()))
        )
        val abilityType = AbilityType(CDef.AbilityType.襲撃)

        // ## Act ##
        // ## Assert ##
        assertThat(abilityDomainService.isUsable(village, participant, abilityType)).isFalse()
    }
}