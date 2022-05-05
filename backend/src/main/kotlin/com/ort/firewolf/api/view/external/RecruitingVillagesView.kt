package com.ort.firewolf.api.view.external

import com.ort.firewolf.domain.model.charachip.Charachips
import com.ort.firewolf.domain.model.village.Village
import java.time.format.DateTimeFormatter

data class RecruitingVillagesView(
    val villageList: List<RecruitingVillageView>
) {

    constructor(
        villageList: List<Village>,
        charachips: Charachips
    ) : this(
        villageList = villageList.map { RecruitingVillageView(it, charachips) }
    )
}

data class RecruitingVillageView(
    val id: Int,
    val name: String,
    val status: String,
    val participantCount: Int,
    val participantCapacity: Int,
    val dayChangeTime: String,
    val startDatetime: String,
    val charachipName: String,
    val sayableTime: String,
    val url: String,
    val organization: String
) {
    constructor(
        village: Village,
        charachips: Charachips
    ) : this(
        id = village.id,
        name = village.name,
        status = village.status.name,
        participantCount = village.participant.count,
        participantCapacity = village.setting.capacity.max,
        dayChangeTime = village.day.latestDay().dayChangeDatetime.toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm")),
        startDatetime = village.setting.time.startDatetime.format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")),
        charachipName = village.setting.charachip.charachipIds.map { id -> charachips.list.first { it.id == id }.name }
            .joinToString("、"),
        sayableTime =
        if (village.setting.time.silentHours == null || village.setting.time.silentHours == 0) "24時間"
        else village.setting.time.let {
            val start = it.startDatetime.plusHours(it.silentHours!!.toLong()).toLocalTime()
            val end = it.startDatetime.toLocalTime()
            val endPrefix = if (start.isAfter(end)) "翌" else ""
            "${start.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endPrefix}${
                end.format(
                    DateTimeFormatter.ofPattern(
                        "HH:mm"
                    )
                )
            }"
        },
        url = "https://firewolf.netlify.app/village?id=${village.id}",
        organization = village.setting.organizations.organization[village.setting.capacity.max] ?: ""
    )
}
