package com.ort.firewolf.application.service

import com.ort.firewolf.domain.model.reserved.ReservedVillage
import com.ort.firewolf.domain.model.reserved.ReservedVillages
import com.ort.firewolf.infrastructure.datasource.reserved.ReservedVillageDataSource
import org.springframework.stereotype.Service

@Service
class ReservedVillageService(
    val reservedVillageDataSource: ReservedVillageDataSource
) {

    fun findReservedVillages(): ReservedVillages {
        return reservedVillageDataSource.findReservedVillages(true)
    }

    fun findReservedVillagesIncludePast(): ReservedVillages {
        return reservedVillageDataSource.findReservedVillages(false)
    }

    fun registerReservedVillage(reservedVillage: ReservedVillage): ReservedVillage {
        return reservedVillageDataSource.registerReservedVillage(reservedVillage)
    }

    fun deleteReservedVillage(id: Int) = reservedVillageDataSource.deleteReservedVillage(id)
}