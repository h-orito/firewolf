package com.ort.firewolf.application.coordinator

import com.ort.dbflute.allcommon.CDef
import com.ort.dbflute.exbhv.MessageBhv
import com.ort.dbflute.exbhv.PlayerBhv
import com.ort.firewolf.FirewolfTest
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.VillageDays
import com.ort.firewolf.domain.model.village.VillageStatus
import com.ort.firewolf.domain.model.village.participant.VillageParticipants
import com.ort.firewolf.domain.model.village.setting.PersonCapacity
import com.ort.firewolf.domain.model.village.setting.VillageCharachip
import com.ort.firewolf.domain.model.village.setting.VillageMessageRestricts
import com.ort.firewolf.domain.model.village.setting.VillageOrganizations
import com.ort.firewolf.domain.model.village.setting.VillagePassword
import com.ort.firewolf.domain.model.village.setting.VillageRules
import com.ort.firewolf.domain.model.village.setting.VillageSettings
import com.ort.firewolf.domain.model.village.setting.VillageTime
import com.ort.firewolf.fw.security.FirewolfUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class DayChangeCoordinatorTest : FirewolfTest() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    lateinit var villageCoordinator: VillageCoordinator
    @Autowired
    lateinit var dayChangeCoordinator: DayChangeCoordinator
    @Autowired
    lateinit var playerBhv: PlayerBhv
    @Autowired
    lateinit var messageBhv: MessageBhv
    @Autowired
    lateinit var villageService: VillageService
    @Autowired
    lateinit var charachipService: CharachipService

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_dayChangeIfNeeded_プロローグ_何もしない() {
        // ## Arrange ##
        var village = registerVillage()

        // ## Act ##
        dayChangeCoordinator.dayChangeIfNeeded(village)

        // ## Assert ##
        village = villageService.findVillage(village.id)
        assertThat(village.status.toCdef()).isEqualTo(CDef.VillageStatus.プロローグ)
        assertThat(village.participant.count).isEqualTo(1)
    }

    @Test
    fun test_dayChangeIfNeeded_プロローグ_進行中へ() {
        // ## Arrange ##
        var village = registerVillage()
        val charas = charachipService.findCharas(village.setting.charachip.charachipId)
        (2..11).forEach {
            villageCoordinator.participate(village.id, it, charas.list[it].id, message = "hoge", isSpectate = false)
        }
        village = villageService.updateVillageDifference(
            village, village.copy(
                setting = village.setting.copy(
                    time = village.setting.time.copy(
                        startDatetime = LocalDateTime.now().minusMinutes(1L)
                    )
                ),
                day = VillageDays(
                    listOf(
                        village.day.latestDay().copy(
                            dayChangeDatetime = LocalDateTime.now().minusMinutes(1L)
                        )
                    )
                )
            )
        )
        village = villageService.findVillage(village.id)

        // ## Act ##
        dayChangeCoordinator.dayChangeIfNeeded(village)

        // ## Assert ##
        village = villageService.findVillage(village.id)
        assertThat(village.status.toCdef()).isEqualTo(CDef.VillageStatus.進行中)
        assertThat(village.participant.count).isEqualTo(11)
        assertThat(village.participant.memberList.all { it.skill != null }).isTrue()
        assertThat(village.day.latestDay().day).isEqualTo(1)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun registerVillage(): Village {
        val paramVillage = createVillageRegisterParam()
        val player = playerBhv.selectByPK(2).get()
        val user = FirewolfUser(player.uid, player.authorityCodeAsAuthority)
        val villageId = villageCoordinator.registerVillage(paramVillage, user)
        return villageService.findVillage(villageId)
    }

    private fun createVillageRegisterParam(): Village {
        return Village(
            id = 1, // dummy
            name = "dummy village name",
            creatorPlayerId = 1,
            status = VillageStatus(CDef.VillageStatus.プロローグ),
            setting = createVillageSettingsParam(),
            participant = VillageParticipants(
                count = 1, // dummy
                memberList = listOf()
            ),
            spectator = VillageParticipants(
                count = 0, // dummy
                memberList = listOf() // dummy
            ),
            day = VillageDays(
                dayList = listOf() // dummy
            ),
            winCamp = null
        )
    }

    private fun createVillageSettingsParam(): VillageSettings {
        return VillageSettings(
            capacity = PersonCapacity(
                min = 10,
                max = 16
            ),
            time = VillageTime(
                termType = CDef.Term.長期.code(),
                prologueStartDatetime = LocalDateTime.now(),
                epilogueStartDatetime = null,
                epilogueDay = null,
                startDatetime = LocalDateTime.now().plusDays(1L),
                dayChangeIntervalSeconds = 86400,
                silentHours = null
            ),
            charachip = VillageCharachip(
                dummyCharaId = 1,
                charachipId = 1
            ),
            organizations = VillageOrganizations(),
            rules = VillageRules(
                openVote = false,
                availableSkillRequest = true,
                availableSpectate = false,
                openSkillInGrave = false,
                visibleGraveMessage = false,
                availableSuddenlyDeath = true,
                availableCommit = true,
                messageRestrict = VillageMessageRestricts()
            ),
            password = VillagePassword(
                joinPasswordRequired = false,
                joinPassword = null
            )
        )
    }
}