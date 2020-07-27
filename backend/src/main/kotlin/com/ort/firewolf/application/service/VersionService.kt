package com.ort.firewolf.application.service

import com.ort.firewolf.domain.model.version.Version
import com.ort.firewolf.infrastructure.datasource.version.VersionDataSource
import org.springframework.stereotype.Service

@Service
class VersionService(
    val versionDataSource: VersionDataSource
) {

    fun findVersion(): Version {
        return versionDataSource.findVersion()
    }
}
