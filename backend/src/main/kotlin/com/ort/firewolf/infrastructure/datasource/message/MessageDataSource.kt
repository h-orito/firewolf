package com.ort.firewolf.infrastructure.datasource.message

import com.ort.dbflute.allcommon.CDef
import com.ort.dbflute.cbean.MessageCB
import com.ort.dbflute.exbhv.MessageBhv
import com.ort.dbflute.exbhv.MessageSendtoBhv
import com.ort.dbflute.exentity.Message
import com.ort.dbflute.exentity.MessageSendto
import com.ort.firewolf.api.controller.VillageController
import com.ort.firewolf.domain.model.message.MessageContent
import com.ort.firewolf.domain.model.message.MessageQuery
import com.ort.firewolf.domain.model.message.MessageTime
import com.ort.firewolf.domain.model.message.MessageType
import com.ort.firewolf.domain.model.message.Messages
import com.ort.firewolf.domain.model.village.Village
import com.ort.firewolf.domain.model.village.participant.VillageParticipant
import com.ort.firewolf.domain.model.village.participant.VillageParticipantName
import com.ort.firewolf.fw.FirewolfDateUtil
import com.ort.firewolf.fw.exception.FirewolfBusinessException
import org.dbflute.cbean.result.PagingResultBean
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.ZoneOffset
import java.util.regex.Pattern

@Repository
class MessageDataSource(
    private val messageBhv: MessageBhv,
    private val messageSendtoBhv: MessageSendtoBhv
) {

    private val logger = LoggerFactory.getLogger(VillageController::class.java)

    companion object {
        val patternMessageTypeMap = mapOf(
            Pattern.compile("^(?![\\+=\\?@\\-\\*a_])(\\d{1,5})") to CDef.MessageType.通常発言,
            Pattern.compile("^\\+(\\d{1,5})") to CDef.MessageType.死者の呻き,
            Pattern.compile("^=(\\d{1,5})") to CDef.MessageType.共鳴発言,
//            Pattern.compile("^\\?(\\d{1,5})") to CDef.MessageType.恋人発言,
            Pattern.compile("^@(\\d{1,5})") to CDef.MessageType.見学発言,
            Pattern.compile("^-(\\d{1,5})") to CDef.MessageType.独り言,
            Pattern.compile("^\\*(\\d{1,5})") to CDef.MessageType.人狼の囁き,
            Pattern.compile("^a(\\d{1,5})") to CDef.MessageType.アクション,
//            Pattern.compile("^_(\\d{1,5})") to CDef.MessageType.念話
        )
    }

    /**
     * 発言取得
     *
     * @param villageId villageId
     * @param villageDayId 村日付ID
     * @param query query
     * @return 発言
     */
    fun findMessages(
        villageId: Int,
        villageDayId: Int,
        query: MessageQuery
    ): Messages {
        if (query.messageTypeList.isEmpty() && !query.includeMonologue && !query.includeSecret) {
            return Messages(listOf())
        }

        val messagePage = messageBhv.selectPage {
            queryPaging(it, query)
            queryMessage(
                cb = it,
                villageId = villageId,
                villageDayId = villageDayId,
                query = query
            )
            if (query.isLatest) {
                it.query().addOrderBy_MessageUnixtimestampMilli_Desc()
            } else {
                it.query().addOrderBy_MessageUnixtimestampMilli_Asc()
            }
        }
        messageBhv.load(messagePage) { it.loadMessageSendto { } }
        return if (query.isLatest) mapMessagesWithLatest(messagePage)
        else mapMessagesWithPaging(messagePage)
    }

    private fun mapMessagesWithPaging(messagePage: PagingResultBean<Message>): Messages =
        Messages(
            list = messagePage.map { convertMessageToMessage(it) },
            allPageCount = messagePage.allPageCount,
            allRecordCount = messagePage.allRecordCount,
            isExistPrePage = messagePage.existsPreviousPage(),
            isExistNextPage = messagePage.existsNextPage(),
            currentPageNum = messagePage.currentPageNumber,
            isLatest = false
        )

    private fun mapMessagesWithLatest(messagePage: PagingResultBean<Message>): Messages =
        Messages(
            list = messagePage.reversed().map { convertMessageToMessage(it) },
            allPageCount = messagePage.allPageCount,
            allRecordCount = messagePage.allRecordCount,
            isExistPrePage = messagePage.existsNextPage(), // 逆順にしているので
            isExistNextPage = false, // 最新なので次はなし
            currentPageNum = null,
            isLatest = true
        )

    /**
     * 最新発言日時取得
     *
     * @param villageId villageId
     * @param messageTypeList 発言種別
     * @param participant 参加情報
     * @return 最新発言日時(unix_datetime_milli)
     */
    fun findLatestMessagesUnixTimeMilli(
        villageId: Int,
        messageTypeList: List<CDef.MessageType>,
        participant: VillageParticipant?
    ): Long {
        val query = MessageQuery(
            from = null,
            pageSize = null,
            pageNum = null,
            keyword = null,
            participant = participant,
            messageTypeList = messageTypeList,
            fromParticipantIdList = null,
            toParticipantIdList = null,
            includeMonologue = false,
            includeSecret = false,
            includePrivateAbility = false,
            isLatest = false
        )
        return messageBhv.selectEntityWithDeletedCheck() {
            queryMessage(it, villageId, null, query)
            it.query().addOrderBy_MessageUnixtimestampMilli_Desc()
            it.fetchFirst(1)
        }.messageUnixtimestampMilli
    }

    /**
     * アンカー発言取得
     *
     * @param villageId villageId
     * @param messageType 発言種別
     * @param messageNumber 発言番号
     * @return 発言
     */
    fun findMessage(
        villageId: Int,
        messageType: CDef.MessageType,
        messageNumber: Int
    ): com.ort.firewolf.domain.model.message.Message? {
        val optMessage = messageBhv.selectEntity {
            it.query().setVillageId_Equal(villageId)
            it.query().setMessageNumber_Equal(messageNumber)
            it.query().setMessageTypeCode_Equal(messageType.code())
        }
        return optMessage.map {
            messageBhv.load(it) { it.loadMessageSendto { } }
            convertMessageToMessage(it)
        }.orElse(null)
    }

    /**
     * 参加者のその日の発言を取得
     *
     * @param villageId villageId
     * @param villageDayId 村日付ID
     * @param participant 参加情報
     * @return 発言
     */
    fun selectParticipateDayMessageList(
        villageId: Int,
        villageDayId: Int,
        participant: VillageParticipant
    ): Map<CDef.MessageType, Int> {
        val messageList = messageBhv.selectList {
            it.query().setVillageId_Equal(villageId)
            it.query().setVillagePlayerId_Equal(participant.id)
            it.query().setVillageDayId_Equal(villageDayId)
        }
        return messageList.groupBy { CDef.MessageType.codeOf(it.messageTypeCode) }.mapValues { it.value.size }
    }

    fun registerMessage(
        village: Village,
        message: com.ort.firewolf.domain.model.message.Message
    ): com.ort.firewolf.domain.model.message.Message {
        val mes = Message()
        val messageTypeCode = message.content.type.code
        mes.villageId = village.id
        mes.villageDayId = message.time.villageDayId
        mes.messageTypeCode = messageTypeCode
        mes.messageContent = message.content.text
        mes.villagePlayerId = message.fromVillageParticipantId
        mes.toVillagePlayerId = message.toVillageParticipantId
        mes.playerId = null // TODO プレイヤー発言を実装する際に実装する
        mes.faceTypeCode = message.content.faceCode
        mes.isConvertDisable = true
        val now = FirewolfDateUtil.currentLocalDateTime()
        mes.messageDatetime = now
        mes.messageUnixtimestampMilli = now.toInstant(ZoneOffset.ofHours(+9)).toEpochMilli()
        message.fromVillageParticipantId?.let { participantId ->
            val participant = village.allParticipants().member(participantId)
            mes.charaName = participant.charaName.name
            mes.charaShortName = participant.charaName.shortName
        }
        message.toVillageParticipantId?.let { participantId ->
            val participant = village.allParticipants().member(participantId)
            mes.toCharaName = participant.charaName.name
            mes.toCharaShortName = participant.charaName.shortName
        }

        // 何回目の発言か
        if (message.content.type.shouldSetCount()) {
            val count: Int = selectMessageTypeCount(
                village.id,
                message.time.villageDayId,
                messageTypeCode,
                message.fromVillageParticipantId
            )
            mes.messageCount = count + 1
        }

        // 発言番号の採番 & insert (3回チャレンジする)
        for (i in 1..3) {
            try {
                mes.messageNumber = selectNextMessageNumber(village.id, message.content.type.code)
                messageBhv.insert(mes)
                insertMessageSendTo(mes)
                return findMessage(
                    villageId = village.id,
                    messageType = CDef.MessageType.codeOf(messageTypeCode),
                    messageNumber = mes.messageNumber
                )!!
            } catch (e: RuntimeException) {
                logger.error(e.message, e)
            }
        }
        throw FirewolfBusinessException("混み合っているため発言に失敗しました。再度発言してください。")
    }

    private fun insertMessageSendTo(m: Message) {
        val splitted = m.messageContent.split(">>")
        if (splitted.size <= 1) {
            return  // >>が含まれていない
        }
        splitted.drop(1).forEach { str ->
            patternMessageTypeMap.forEach { (pattern: Pattern, messageType: CDef.MessageType) ->
                val matcher = pattern.matcher(str)
                if (matcher.find()) {
                    val number = matcher.group(1).toInt()
                    val optMessage =
                        messageBhv.selectEntity {
                            it.query().setVillageId_Equal(m.villageId)
                            it.query().setMessageTypeCode_Equal(messageType.code())
                            it.query().setMessageNumber_Equal(number)
                        }
                    if (!optMessage.isPresent || optMessage.get().villagePlayerId == null) {
                        return@forEach
                    }
                    val sendTo = MessageSendto()
                    sendTo.villageId = m.villageId
                    sendTo.messageTypeCode = m.messageTypeCode
                    sendTo.messageNumber = m.messageNumber
                    sendTo.villagePlayerId = optMessage.get().villagePlayerId
                    val optEntity =
                        messageSendtoBhv.selectEntity {
                            it.query().setVillageId_Equal(sendTo.villageId)
                            it.query().setMessageTypeCode_Equal(sendTo.messageTypeCode)
                            it.query().setMessageNumber_Equal(sendTo.messageNumber)
                            it.query().setVillagePlayerId_Equal(sendTo.villagePlayerId)
                        }
                    if (!optEntity.isPresent) {
                        messageSendtoBhv.insert(sendTo)
                    }
                }
            }
        }
    }

    /**
     * 差分更新
     */
    fun updateDifference(village: Village, before: Messages, after: Messages) {
        // 追加しかないのでbeforeにないindexから追加していく
        after.list.drop(before.list.size).forEach {
            registerMessage(village, it)
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun selectNextMessageNumber(villageId: Int, messageType: String): Int {
        val maxNumber: Int = messageBhv.selectScalar(Int::class.java).max {
            it.specify().columnMessageNumber()
            it.query().setVillageId_Equal(villageId)
            it.query().setMessageTypeCode_Equal(messageType)
        }.orElse(0)
        return maxNumber + 1
    }

    private fun selectMessageTypeCount(
        villageId: Int,
        villageDayId: Int,
        messageTypeCode: String,
        villageParticipantId: Int?
    ): Int {
        return messageBhv.selectCount {
            it.query().setVillageId_Equal(villageId)
            it.query().setVillageDayId_Equal(villageDayId)
            it.query().setMessageTypeCode_Equal(messageTypeCode)
            it.query().setVillagePlayerId_Equal(villageParticipantId)
        }
    }

    private fun convertMessageToMessage(message: Message): com.ort.firewolf.domain.model.message.Message {
        return com.ort.firewolf.domain.model.message.Message(
            fromVillageParticipantId = message.villagePlayerId,
            fromCharacterName = message.charaName?.let {
                VillageParticipantName(
                    name = it,
                    shortName = message.charaShortName
                )
            },
            toVillageParticipantId = message.toVillagePlayerId,
            toCharacterName = message.toCharaName?.let {
                VillageParticipantName(
                    name = it,
                    shortName = message.toCharaShortName
                )
            },
            time = MessageTime(
                villageDayId = message.villageDayId,
                datetime = message.messageDatetime,
                unixTimeMilli = message.messageUnixtimestampMilli
            ),
            content = MessageContent(
                type = MessageType(
                    code = message.messageTypeCode,
                    name = CDef.MessageType.codeOf(message.messageTypeCode).alias()
                ),
                num = message.messageNumber,
                count = message.messageCount,
                text = message.messageContent,
                faceCode = message.faceTypeCode
            ),
            sendToParticipantIds = message.messageSendtoList.map { it.villagePlayerId }
        )
    }

    private fun queryMessage(
        cb: MessageCB,
        villageId: Int,
        villageDayId: Int?,
        query: MessageQuery
    ) {
        cb.query().setVillageId_Equal(villageId)
        if (villageDayId != null) cb.query().setVillageDayId_Equal(villageDayId)
        // 発言種別
        queryMessageType(cb, query)

        query.from?.let { cb.query().setMessageUnixtimestampMilli_GreaterThan(it) }
        query.keyword?.let {
            cb.query().setMessageContent_LikeSearch(it) { op -> op.splitByBlank().likeContain().asOrSplit() }
        }
        if (!query.fromParticipantIdList.isNullOrEmpty()) {
            cb.query().setVillagePlayerId_InScope(query.fromParticipantIdList)
        }
        if (!query.toParticipantIdList.isNullOrEmpty()) {
            cb.orScopeQuery { orCB ->
                orCB.query().existsMessageSendto { sendToCB ->
                    sendToCB.query().setVillagePlayerId_InScope(query.toParticipantIdList)
                }
                orCB.query().setToVillagePlayerId_InScope(query.toParticipantIdList)
            }
        }
    }

    private fun queryMessageType(
        cb: MessageCB,
        query: MessageQuery,
    ) {
        if (query.participant == null) {
            if (query.messageTypeList.isNotEmpty()) {
                cb.query().setMessageTypeCode_InScope(query.messageTypeList.map { it.code() })
            }
            return
        }
        if (query.messageTypeList.isNotEmpty()) {
            if (query.includeMonologue || query.includeSecret || query.includePrivateAbility) {
                cb.orScopeQuery { orCB ->
                    orCB.query().setMessageTypeCode_InScope(query.messageTypeList.map { it.code() })
                    queryMyself(orCB, query, query.participant)
                }
            } else {
                cb.query().setMessageTypeCode_InScope(query.messageTypeList.map { it.code() })
            }
        } else {
            if (query.includeMonologue || query.includeSecret || query.includePrivateAbility) {
                cb.orScopeQuery { orCB -> queryMyself(orCB, query, query.participant) }
            }
        }
    }

    private fun queryMyself(cb: MessageCB, query: MessageQuery, myself: VillageParticipant) {
        if (query.includeMonologue) {
            cb.orScopeQueryAndPart { andCB -> queryMyMonologue(andCB, myself.id) }
        }
        if (query.includeSecret) {
            cb.orScopeQueryAndPart { andCB -> queryMySecret(andCB, myself.id) }
            cb.orScopeQueryAndPart { andCB -> querySecretToMe(andCB, myself.id) }
        }
        if (query.includePrivateAbility) {
            cb.orScopeQueryAndPart { andCB -> queryMyPrivateAbility(andCB, myself.id) }
        }
    }

    private fun queryMyMonologue(cb: MessageCB, id: Int) {
        cb.query().setVillagePlayerId_Equal(id)
        cb.query().setMessageTypeCode_Equal(CDef.MessageType.独り言.code())
    }

    private fun queryMySecret(cb: MessageCB, id: Int) {
        cb.query().setVillagePlayerId_Equal(id)
        cb.query().setMessageTypeCode_Equal(CDef.MessageType.秘話.code())
    }

    private fun querySecretToMe(cb: MessageCB, id: Int) {
        cb.query().setToVillagePlayerId_Equal(id)
        cb.query().setMessageTypeCode_Equal(CDef.MessageType.秘話.code())
    }

    private fun queryMyPrivateAbility(cb: MessageCB, id: Int) {
        cb.query().setVillagePlayerId_Equal(id)
        cb.query().setMessageTypeCode_InScope(MessageQuery.personalPrivateAbilityList.map { it.code() })
    }

    private fun queryPaging(cb: MessageCB, query: MessageQuery) {
        when {
            !query.isPaging() -> cb.paging(100000, 1)
            query.isLatest -> cb.paging(query.pageSize!!, 1)
            query.pageNum != null && query.pageSize != null -> cb.paging(query.pageSize, query.pageNum)
            query.pageSize != null -> cb.paging(query.pageSize, 100000)
            else -> cb.paging(100000, 1)
        }
    }
}
