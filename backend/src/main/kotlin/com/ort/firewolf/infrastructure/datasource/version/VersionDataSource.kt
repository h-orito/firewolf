package com.ort.firewolf.infrastructure.datasource.version

import com.ort.dbflute.exbhv.AppVersionBhv
import com.ort.firewolf.domain.model.version.Version
import org.springframework.stereotype.Repository

@Repository
class VersionDataSource(
    val appVersionBhv: AppVersionBhv
) {

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    fun findVersion(): Version {
        val version = appVersionBhv.selectEntityWithDeletedCheck {
            it.fetchFirst(1)
        }
        return Version(
            clientVersion = version.clientVersion
        )
    }
}