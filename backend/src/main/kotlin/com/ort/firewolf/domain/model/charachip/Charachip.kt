package com.ort.firewolf.domain.model.charachip

data class Charachip(
    val id: Int,
    val name: String,
    val designer: Designer,
    val descriptionUrl: String,
    val charaIdList: List<Int>,
    val isAvailableChangeName: Boolean
)