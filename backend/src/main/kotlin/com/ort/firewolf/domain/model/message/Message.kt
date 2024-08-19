package com.ort.firewolf.domain.model.message

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.participant.VillageParticipantName
import com.ort.firewolf.fw.FirewolfDateUtil

data class Message(
    val fromVillageParticipantId: Int?,
    val fromCharacterName: VillageParticipantName?,
    val toVillageParticipantId: Int?,
    val toCharacterName: VillageParticipantName?,
    val time: MessageTime,
    val content: MessageContent,
    val sendToParticipantIds: List<Int> = emptyList()
) {
    fun shouldNotify(): Boolean = content.shouldNotify()

    companion object {

        fun createSayMessage(
            from: VillageParticipant,
            villageDayId: Int,
            messageContent: MessageContent,
            to: VillageParticipant? = null
        ): Message = Message(
            fromVillageParticipantId = from.id,
            fromCharacterName = from.charaName,
            toVillageParticipantId = to?.id,
            toCharacterName = to?.charaName,
            time = MessageTime(
                villageDayId = villageDayId,
                datetime = FirewolfDateUtil.currentLocalDateTime(), // dummy
                unixTimeMilli = 0L // dummy
            ),
            content = messageContent
        )

        // 別途情報取得して表示させるので、参加者一覧であることだけわかればok
        fun createParticipantsMessage(villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.参加者一覧), "読み込み中...", villageDayId)

        fun createPublicSystemMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.公開システムメッセージ), text, villageDayId)

        fun createPrivateSystemMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.非公開システムメッセージ), text, villageDayId)

        fun createSeerPrivateMessage(text: String, villageDayId: Int, participant: VillageParticipant): Message =
            createSystemMessage(MessageType(CDef.MessageType.白黒占い結果), text, villageDayId, participant)

        fun createWisePrivateMessage(text: String, villageDayId: Int, participant: VillageParticipant): Message =
            createSystemMessage(MessageType(CDef.MessageType.役職占い結果), text, villageDayId, participant)

        fun createPsychicPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.白黒霊視結果), text, villageDayId)

        fun createGuruPsychicPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.役職霊視結果), text, villageDayId)

        fun createAttackPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.襲撃結果), text, villageDayId)

        fun createFanaticPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.狂信者人狼確認メッセージ), text, villageDayId)

        fun createMasonPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.共有相互確認メッセージ), text, villageDayId)

        fun createSympathizerPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.共鳴相互確認メッセージ), text, villageDayId)

        fun createFoxPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.妖狐メッセージ), text, villageDayId)

        fun createCreatorSayMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.村建て発言), text, villageDayId)

        fun createAutopsyPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.検死結果), text, villageDayId)

        fun createPrivateLoversMessage(
            text: String,
            villageDayId: Int,
            villageParticipant: VillageParticipant
        ): Message =
            createSystemMessage(MessageType(CDef.MessageType.恋人メッセージ), text, villageDayId, villageParticipant)

        fun createPrivateAbilityMessage(
            text: String,
            villageDayId: Int,
            villageParticipant: VillageParticipant,
        ): Message =
            createSystemMessage(MessageType(CDef.MessageType.能力行使メッセージ), text, villageDayId, villageParticipant)

        // ===================================================================================
        //                                                                        Assist Logic
        //                                                                        ============
        private fun createSystemMessage(
            messageType: MessageType,
            text: String,
            villageDayId: Int,
            from: VillageParticipant? = null
        ): Message = Message(
            fromVillageParticipantId = from?.id,
            fromCharacterName = null,
            toVillageParticipantId = null,
            toCharacterName = null,
            time = MessageTime(
                villageDayId = villageDayId,
                datetime = FirewolfDateUtil.currentLocalDateTime(), // dummy
                unixTimeMilli = 0L // dummy
            ),
            content = MessageContent(
                type = messageType,
                num = 0, // dummy
                count = 0, // dummy
                text = text,
                faceCode = null
            )
        )
    }
}