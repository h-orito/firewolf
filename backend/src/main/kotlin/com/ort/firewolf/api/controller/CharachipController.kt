package com.ort.firewolf.api.controller

import com.ort.firewolf.api.form.CharachipsForm
import com.ort.firewolf.api.view.charachip.CharachipView
import com.ort.firewolf.api.view.charachip.CharachipsView
import com.ort.firewolf.application.service.CharachipService
import com.ort.firewolf.domain.model.charachip.Chara
import com.ort.firewolf.domain.model.charachip.Charachip
import com.ort.firewolf.domain.model.charachip.Charachips
import com.ort.firewolf.domain.model.charachip.Charas
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CharachipController(
    val charachipService: CharachipService
) {

    // ===================================================================================
    //                                                                             Execute
    //                                                                           =========
    @GetMapping("/charachip/list")
    fun charachipList(): CharachipsView {
        val charachips: Charachips = charachipService.findCharaChips()
        val charas: Charas = charachipService.findCharas(charachips)
        return CharachipsView(
            charachips = charachips,
            charas = charas
        )
    }

    @GetMapping("/charachips")
    fun charachips(form: CharachipsForm): CharachipsView {
        val charachips: Charachips = charachipService.findCharachips(form.charachipIds!!)
        val charas: Charas = charachipService.findCharas(charachips)
        return CharachipsView(
            charachips = charachips,
            charas = charas
        )
    }

    @GetMapping("/charachips/{charaChipId}")
    fun charachip(@PathVariable("charaChipId") charaChipId: Int): CharachipView {
        val charachip: Charachip = charachipService.findCharaChip(charaChipId)
        val charas: Charas = charachipService.findCharas(charachip.id)
        return CharachipView(
            charachip = charachip,
            charas = charas
        )
    }

    @GetMapping("/charas")
    fun charas(form: CharachipsForm): Charas {
        return charachipService.findCharas(form.charachipIds!!)
    }

    @GetMapping("/chara/{charaId}")
    fun chara(@PathVariable("charaId") charaId: Int): Chara {
        return charachipService.findChara(charaId)
    }

    @GetMapping("/charachip/{charaChipId}/dummychara")
    fun dummyChara(@PathVariable("charaChipId") charaChipId: Int): Chara {
        return charachipService.findDummyChara(charaChipId)
    }
}
