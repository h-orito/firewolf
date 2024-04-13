package com.ort.firewolf.domain.model.charachip

import com.fasterxml.jackson.annotation.JsonProperty

data class Charachip(
    val id: Int,
    val name: String,
    val designer: Designer,
    val descriptionUrl: String,
    val charaIdList: List<Int>,
    @JsonProperty("isAvailableChangeName") val isAvailableChangeName: Boolean
)