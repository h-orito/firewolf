package com.ort.firewolf.domain.model.village.setting

data class VillageTags(
    val list: List<String>
) {
    fun isR15() = list.contains("R15")
    fun isR18() = list.contains("R18")

    fun existsDifference(tags: VillageTags): Boolean {
        return tags.list.size != list.size
                || !tags.list.containsAll(list)
    }
}