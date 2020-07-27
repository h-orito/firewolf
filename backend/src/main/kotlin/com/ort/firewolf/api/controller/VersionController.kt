package com.ort.firewolf.api.controller

import com.ort.firewolf.application.service.VersionService
import com.ort.firewolf.domain.model.version.Version
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController(
    val versionService: VersionService
) {

    // ===================================================================================
    //                                                                             Execute
    //                                                                           =========
    /**
     * クライアントの想定バージョンを取得
     */
    @GetMapping("/version")
    fun version(
    ): Version {
        val version = versionService.findVersion()
        return Version(
            clientVersion = version.clientVersion
        )
    }
}