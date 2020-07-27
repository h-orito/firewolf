package com.ort.firewolf.dummy

import com.ort.dbflute.exbhv.PlayerBhv
import com.ort.firewolf.application.coordinator.VillageCoordinator
import com.ort.firewolf.application.service.VillageService
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.fw.security.FirewolfUser
import org.springframework.stereotype.Service

@Service
class DummyVillageService(
    private val playerBhv: PlayerBhv,
    private val villageCoordinator: VillageCoordinator,
    private val villageService: VillageService
) {

    // G16編成、沈黙時間なし
    fun createG16PrologueVillage(): Village {
        val player = playerBhv.selectByPK(2).get()
        val user = FirewolfUser(player.uid, player.authorityCodeAsAuthority)
        val villageId = villageCoordinator.registerVillage(
            DummyDomainModelCreator.createDummyVillageRegisterParam(),
            user
        )
        return villageService.findVillage(villageId)
    }
}
