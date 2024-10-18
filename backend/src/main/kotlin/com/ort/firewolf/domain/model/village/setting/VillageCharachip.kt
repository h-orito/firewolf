package com.ort.firewolf.domain.model.village.setting

data class VillageCharachip(
    val dummyCharaId: Int,
    val dummyCharaShortName: String,
    val dummyCharaName: String,
    val dummyCharaDay0Message: String,
    val dummyCharaDay1Message: String?,
    val charachipIds: List<Int>
) {
    fun existsDifference(charachip: VillageCharachip): Boolean {
        return dummyCharaId != charachip.dummyCharaId
                || dummyCharaShortName != charachip.dummyCharaShortName
                || dummyCharaName != charachip.dummyCharaName
                || dummyCharaDay0Message != charachip.dummyCharaDay0Message
                || dummyCharaDay1Message != charachip.dummyCharaDay1Message
                || charachipIds != charachip.charachipIds
    }

    companion object {
        operator fun invoke(
            dummyCharaId: Int?,
            dummyCharaShortName: String?,
            dummyCharaName: String?,
            dummyCharaDay0Message: String?,
            dummyCharaDay1Message: String?,
            charachipIds: List<Int>?
        ): VillageCharachip {
            requireNotNull(dummyCharaId)
            requireNotNull(dummyCharaShortName)
            requireNotNull(dummyCharaName)
            requireNotNull(dummyCharaDay0Message)
            requireNotNull(charachipIds)
            return VillageCharachip(
                dummyCharaId = dummyCharaId,
                dummyCharaShortName = dummyCharaShortName,
                dummyCharaName = dummyCharaName,
                dummyCharaDay0Message = dummyCharaDay0Message,
                dummyCharaDay1Message = dummyCharaDay1Message,
                charachipIds = charachipIds
            )
        }
    }
}