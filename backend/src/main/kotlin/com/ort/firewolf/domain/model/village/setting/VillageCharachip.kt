package com.ort.firewolf.domain.model.village.setting

data class VillageCharachip(
    val dummyCharaId: Int,
    val charachipIds: List<Int>
) {
    companion object {
        operator fun invoke(
            dummyCharaId: Int?,
            charachipIds: List<Int>?
        ): VillageCharachip {
            requireNotNull(dummyCharaId)
            requireNotNull(charachipIds)
            return VillageCharachip(
                dummyCharaId = dummyCharaId,
                charachipIds = charachipIds
            )
        }
    }
}