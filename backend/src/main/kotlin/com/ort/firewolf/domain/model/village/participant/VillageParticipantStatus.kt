package com.ort.firewolf.domain.model.village.participant

data class VillageParticipantStatus(
    val loverIdList: List<Int> = emptyList(),
) {
    fun hasLover(): Boolean = loverIdList.isNotEmpty()

    fun isSame(other: VillageParticipantStatus): Boolean {
        return loverIdList.size == other.loverIdList.size
                && loverIdList.all { other.loverIdList.contains(it) }
    }

    fun addLover(id: Int): VillageParticipantStatus {
        return copy(loverIdList = (loverIdList + id).distinct())
    }
}