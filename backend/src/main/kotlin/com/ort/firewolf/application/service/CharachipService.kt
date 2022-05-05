package com.ort.firewolf.application.service

import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charachip
import com.ort.firewolf.domain.model.charachip.Charachips
import com.ort.firewolf.domain.model.charachip.Charas
import com.ort.firewolf.infrastructure.datasource.chara.CharaDataSource
import com.ort.firewolf.infrastructure.datasource.charachip.CharachipDataSource
import org.springframework.stereotype.Service

@Service
class CharachipService(
    private val charachipDataSource: CharachipDataSource,
    private val charaDataSource: CharaDataSource
) {

    fun findCharaChips(): Charachips = charachipDataSource.findCharachips()

    fun findCharachips(ids: List<Int>): Charachips = charachipDataSource.findCharachips(ids)

    fun findCharaChip(charachipId: Int): Charachip = charachipDataSource.findCharachip(charachipId)

    fun findChara(charaId: Int): Chara = charaDataSource.findChara(charaId)

    fun findCharas(charachips: Charachips): Charas = charaDataSource.findCharas(charachips)

    fun findCharas(charachipId: Int): Charas = charaDataSource.findCharas(charachipId)

    fun findCharas(charachipIdList: List<Int>): Charas = charaDataSource.findCharas(charachipIdList)

    fun findDummyChara(charaChipId: Int): Chara = charaDataSource.findDummyChara(charaChipId)
}