package com.ort.firewolf.domain.model.message

import com.ort.dbflute.allcommon.CDef

data class MessageType(
    val code: String,
    val name: String
) {

    constructor(
        cdefMessageType: CDef.MessageType
    ) : this(
        code = cdefMessageType.code(),
        name = cdefMessageType.alias()
    )

    companion object {
        val sayTypeList = listOf(
            CDef.MessageType.見学発言,
            CDef.MessageType.独り言,
            CDef.MessageType.死者の呻き,
            CDef.MessageType.恋人発言,
            CDef.MessageType.人狼の囁き,
            CDef.MessageType.共鳴発言,
            CDef.MessageType.通常発言,
            CDef.MessageType.秘話,
            CDef.MessageType.アクション
        )

        val commonSystemTypeList = listOf(
            CDef.MessageType.白黒霊視結果,
            CDef.MessageType.役職霊視結果,
            CDef.MessageType.襲撃結果,
            CDef.MessageType.共有相互確認メッセージ,
            CDef.MessageType.共鳴相互確認メッセージ,
            CDef.MessageType.狂信者人狼確認メッセージ,
            CDef.MessageType.妖狐メッセージ,
            CDef.MessageType.検死結果
        )

        val everyoneAllowedMessageTypeList = listOf(
            CDef.MessageType.公開システムメッセージ,
            CDef.MessageType.通常発言,
            CDef.MessageType.村建て発言,
            CDef.MessageType.アクション
        )

        val requiredCountTypeList: List<CDef.MessageType> = listOf(
            CDef.MessageType.通常発言,
            CDef.MessageType.死者の呻き,
            CDef.MessageType.独り言,
            CDef.MessageType.人狼の囁き,
            CDef.MessageType.見学発言,
            CDef.MessageType.共鳴発言
        )

        val owlViewableSayTypeList = listOf(
            CDef.MessageType.人狼の囁き,
            CDef.MessageType.共鳴発言,
        )
    }

    fun toCdef(): CDef.MessageType = CDef.MessageType.codeOf(code)
    fun isSayType(): Boolean = sayTypeList.contains(this.toCdef())
    fun isOwlViewableType(): Boolean = owlViewableSayTypeList.contains(this.toCdef())

    // 何回目の発言かを登録すべきか
    fun shouldSetCount(): Boolean {
        val cdefMessageType = this.toCdef()
        return requiredCountTypeList.contains(cdefMessageType)
    }
}

fun CDef.MessageType.toModel(): MessageType = MessageType(this)