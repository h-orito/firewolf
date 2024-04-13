package com.ort.firewolf.infrastructure.datasource.player

import com.ort.dbflute.exbhv.PlayerBhv
import com.ort.dbflute.exbhv.PlayerDetailBhv
import com.ort.dbflute.exbhv.TwitterUserBhv
import com.ort.dbflute.exentity.Player
import com.ort.dbflute.exentity.PlayerDetail
import com.ort.dbflute.exentity.TwitterUser
import com.ort.firewolf.domain.model.player.Players
import org.springframework.stereotype.Repository

@Repository
class PlayerDataSource(
    private val playerBhv: PlayerBhv,
    private val playerDetailBhv: PlayerDetailBhv,
    private val twitterUserBhv: TwitterUserBhv
) {

    fun findPlayer(id: Int): com.ort.firewolf.domain.model.player.Player {
        val player = playerBhv.selectEntityWithDeletedCheck {
            it.setupSelect_TwitterUserAsOne()
            it.setupSelect_PlayerDetailAsOne()
            it.query().setPlayerId_Equal(id)
        }
        playerBhv.load(player) {
            it.loadVillage { } // creator village
            it.loadVillagePlayer { vpCB ->
                vpCB.setupSelect_Village()
                vpCB.query().setIsGone_Equal(false) // participant village
            }
        }
        return convertPlayerToPlayer(player)
    }

    fun findPlayer(uid: String): com.ort.firewolf.domain.model.player.Player {
        val player = playerBhv.selectEntityWithDeletedCheck {
            it.setupSelect_TwitterUserAsOne()
            it.setupSelect_PlayerDetailAsOne()
            it.query().setUid_Equal(uid)
        }
        playerBhv.load(player) {
            it.loadVillage { } // creator village
            it.loadVillagePlayer { vpCB ->
                vpCB.setupSelect_Village()
                vpCB.query().setIsGone_Equal(false) // participant village
            }
        }
        return convertPlayerToPlayer(player)
    }

    fun findPlayers(villageId: Int): Players {
        val playerList = playerBhv.selectList {
            it.setupSelect_TwitterUserAsOne()
            it.setupSelect_PlayerDetailAsOne()
            it.query().existsVillagePlayer {
                it.query().setVillageId_Equal(villageId)
            }
        }
        return Players(list = playerList.map { convertPlayerToSimplePlayer(it) })
    }

    fun findPlayers(playerIdList: List<Int>): Players {
        if (playerIdList.isEmpty()) return Players(listOf())
        val playerList = playerBhv.selectList {
            it.setupSelect_TwitterUserAsOne()
            it.setupSelect_PlayerDetailAsOne()
            it.query().setPlayerId_InScope(playerIdList)
        }
        return Players(list = playerList.map { convertPlayerToSimplePlayer(it) })
    }

    fun update(uid: String, nickname: String, twitterUserName: String?, twitterUserId: String?) {
        val existingPlayer = findPlayer(uid)
        // 既に自分で変更している可能性があるので、名無しのままの場合のみ変更する
        if (existingPlayer.nickname == "名無し") {
            val player = Player()
            player.uniqueBy(uid)
            player.nickname = nickname.ifEmpty { "名無し" }
            twitterUserId?.let { player.registerTrace = "twitterId: $it" }
            playerBhv.update(player)
        }
        twitterUserName?.let {
            val twitterUser = TwitterUser()
            twitterUser.playerId = existingPlayer.id
            twitterUser.twitterUserName = twitterUserName

            val existing = twitterUserBhv.selectByPK(existingPlayer.id)
            if (existing.isPresent) {
                twitterUserBhv.update(twitterUser)
            } else {
                twitterUserBhv.insert(twitterUser)
            }
        }
    }

    fun updateDetail(
        uid: String,
        nickname: String,
        otherSiteName: String?,
        introduction: String?
    ) {
        val player = Player()
        player.uniqueBy(uid)
        player.nickname = nickname.ifEmpty { "名無し" }
        playerBhv.update(player)

        val detail = PlayerDetail()
        detail.otherSiteName = otherSiteName
        detail.introduction = introduction
        val entity = playerDetailBhv.selectEntity {
            it.query().queryPlayer().setUid_Equal(uid)
        }
        if (entity.isPresent) {
            playerDetailBhv.queryUpdate(detail) {
                it.query().queryPlayer().setUid_Equal(uid)
            }
        } else {
            val player = playerBhv.selectByUniqueOf(uid)
            detail.playerId = player.get().playerId
            playerDetailBhv.insert(detail)
        }
    }

    fun updateDifference(before: Players, after: Players) {
        // player
        after.list.forEach {
            val beforePlayer = before.list.first { bP -> bP.id == it.id }
            if (it.existsDifference(beforePlayer)) {
                val player = Player()
                player.playerId = it.id
                player.isRestrictedParticipation = it.isRestrictedParticipation
                playerBhv.update(player)
            }
        }
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private fun convertPlayerToPlayer(player: Player): com.ort.firewolf.domain.model.player.Player {
        return com.ort.firewolf.domain.model.player.Player(
            id = player.playerId,
            nickname = player.nickname,
            twitterUserName = player.twitterUserAsOne.map { it.twitterUserName }.orElse(null),
            otherSiteName = player.playerDetailAsOne.map { it.otherSiteName }.orElse(null),
            introduction = player.playerDetailAsOne.map { it.introduction }.orElse(null),
            isRestrictedParticipation = player.isRestrictedParticipation,
            shouldCheckAccessInfo = player.shouldCheckAccessInfo,
            participateProgressVillageIdList = player.villagePlayerList.filter {
                !it.village.get().villageStatusCodeAsVillageStatus.isSolvedVillage
            }.map { it.villageId },
            participateFinishedVillageIdList = player.villagePlayerList.filter {
                it.village.get().villageStatusCodeAsVillageStatus.isSolvedVillage
            }.map { it.villageId },
            createProgressVillageIdList = player.villageList.filter {
                !it.villageStatusCodeAsVillageStatus.isSolvedVillage
            }.map { it.villageId },
            createFinishedVillageIdList = player.villageList.filter {
                it.villageStatusCodeAsVillageStatus.isSolvedVillage
            }.map { it.villageId }
        )
    }

    private fun convertPlayerToSimplePlayer(player: Player): com.ort.firewolf.domain.model.player.Player {
        return com.ort.firewolf.domain.model.player.Player(
            id = player.playerId,
            nickname = player.nickname,
            twitterUserName = player.twitterUserAsOne.map { it.twitterUserName }.orElse(null),
            otherSiteName = player.playerDetailAsOne.map { it.otherSiteName }.orElse(null),
            introduction = player.playerDetailAsOne.map { it.introduction }.orElse(null),
            isRestrictedParticipation = player.isRestrictedParticipation,
            shouldCheckAccessInfo = player.shouldCheckAccessInfo
        )
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * 絵文字を除く文字列を返す
     * @param text
     * @return 4byte文字を除いた文字列
     */
    private fun removeSurrogate(text: String): String {
        return text.chunked(1).filter { c ->
            !c.matches("[\\uD800-\\uDFFF]".toRegex())
        }.joinToString(separator = "")
    }
}
