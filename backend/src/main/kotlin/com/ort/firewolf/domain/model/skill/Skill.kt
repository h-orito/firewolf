package com.ort.firewolf.domain.model.skill

import com.ort.dbflute.allcommon.CDef
import com.ort.firewolf.domain.model.ability.AbilityType
import com.ort.firewolf.domain.model.ability.AbilityTypes
import com.ort.firewolf.domain.model.camp.Camp
import com.ort.firewolf.domain.model.camp.toModel
import com.ort.firewolf.domain.model.message.MessageType

data class Skill(
    val code: String,
    val name: String,
    val shortName: String,
    val winJudgeCamp: Camp,
    val abilityList: List<AbilityType>,
    val manualAbilityList: List<AbilityType>,
    val isDivineResultWolf: Boolean,
    val isPsychicResultWolf: Boolean,
    val sayableSkillMessageTypeList: List<MessageType>,
    val viewableSkillMessageTypeList: List<MessageType>,
    val countCamp: Camp?,
    val description: String
) {

    constructor(
        cdefSkill: CDef.Skill
    ) : this(
        code = cdefSkill.code(),
        name = cdefSkill.alias(),
        shortName = cdefSkill.shortName(),
        winJudgeCamp = Camp(CDef.Camp.codeOf(cdefSkill.campCode())),
        abilityList = Companion.getAbilities(cdefSkill).list,
        manualAbilityList = getManualAbilities(cdefSkill).list,
        isDivineResultWolf = cdefSkill.isDivineResultWolf,
        isPsychicResultWolf = cdefSkill.isPsychicResultWolf,
        sayableSkillMessageTypeList = getSayableMessageTypeList(cdefSkill),
        viewableSkillMessageTypeList = getViewableMessageTypeList(cdefSkill),
        countCamp = getCountCamp(cdefSkill),
        description = cdefSkill.description()
    )

    fun camp(): Camp = CDef.Camp.codeOf(toCdef().campCode()).toModel()

    fun isViewableWerewolfSay(): Boolean = toCdef().isViewableWerewolfSay
    fun isAvailableWerewolfSay(): Boolean = toCdef().isAvailableWerewolfSay
    fun isViewableSympathizeSay(): Boolean = toCdef().isViewableSympathizeSay
    fun isAvailableSympathizeSay(): Boolean = toCdef().isAvailableSympathizeSay

    fun canRecognizeWolf(): Boolean = toCdef().isRecognizableWolf
    fun canRecognizeEachMason(): Boolean = toCdef().isRecognizableEachMason
    fun canRecognizeEachSympathizer(): Boolean = toCdef().isRecognizableEachSympathizer
    fun canRecognizeFoxs(): Boolean = toCdef().isRecognizableFox
    fun hasAutopsyAbility(): Boolean = toCdef().isHasAutopsyAbility
    fun hasPsychicAbility(): Boolean = toCdef().isHasPsychicAbility
    fun hasGuruPsychicAbility(): Boolean = toCdef().isHasGuruPsychicAbility
    fun hasAttackAbility(): Boolean = toCdef().isHasAttackAbility

    fun isFoxCount(): Boolean = Skills.foxs.list.any { it.code == code }

    companion object {

        private val skillAbilityTypeListMap = mapOf(
            CDef.Skill.人狼 to listOf(CDef.AbilityType.襲撃),
            CDef.Skill.呪狼 to listOf(CDef.AbilityType.襲撃),
            CDef.Skill.智狼 to listOf(CDef.AbilityType.襲撃),
            CDef.Skill.占い師 to listOf(CDef.AbilityType.占い),
            CDef.Skill.賢者 to listOf(CDef.AbilityType.占い),
            CDef.Skill.狩人 to listOf(CDef.AbilityType.護衛),
            CDef.Skill.風来狩人 to listOf(CDef.AbilityType.風来護衛)
        )

        // 説明書専用
        private val skillAbilityListMapForManual = mapOf(
            CDef.Skill.占い師 to listOf(AbilityType(CDef.AbilityType.占い)),
            CDef.Skill.賢者 to listOf(AbilityType("WISEDIVINE", "役職占い")),
            CDef.Skill.狩人 to listOf(AbilityType(CDef.AbilityType.護衛)),
            CDef.Skill.風来狩人 to listOf(AbilityType(CDef.AbilityType.風来護衛)),
            CDef.Skill.霊能者 to listOf(AbilityType("PSYCHIC", "霊視")),
            CDef.Skill.導師 to listOf(AbilityType("GURUPSYCHIC", "役職霊視")),
            CDef.Skill.検死官 to listOf(AbilityType("AUTOPSY", "検死")),
            CDef.Skill.猫又 to listOf(AbilityType("FORCESUICIDE", "道連れ")),
            CDef.Skill.パン屋 to listOf(AbilityType("BAKE", "パン焼き")),
            CDef.Skill.人狼 to listOf(AbilityType(CDef.AbilityType.襲撃)),
            CDef.Skill.呪狼 to listOf(AbilityType(CDef.AbilityType.襲撃)),
            CDef.Skill.智狼 to listOf(AbilityType("WISEATTACK", "襲撃占い"))
        )

        fun skillByShortName(shortName: String): Skill? {
            val cdefSkill: CDef.Skill = CDef.Skill.listAll().firstOrNull {
                it.shortName() == shortName
            } ?: return null
            return Skill(cdefSkill)
        }

        fun getAbilities(cdefSkill: CDef.Skill): AbilityTypes {
            val cdefAbilityList = skillAbilityTypeListMap[cdefSkill] ?: return AbilityTypes(listOf())
            return AbilityTypes(cdefAbilityList.map { AbilityType(it) })
        }

        fun getManualAbilities(cdefSkill: CDef.Skill): AbilityTypes {
            return AbilityTypes(skillAbilityListMapForManual[cdefSkill] ?: listOf())
        }

        fun getSayableMessageTypeList(cdefSkill: CDef.Skill): List<MessageType> {
            val list = mutableListOf<MessageType>()
            // 囁き
            if (cdefSkill.isAvailableWerewolfSay) list.add(MessageType(CDef.MessageType.人狼の囁き))
            // 共鳴
            if (cdefSkill.isAvailableSympathizeSay) list.add(MessageType(CDef.MessageType.共鳴発言))

            return list
        }

        fun getViewableMessageTypeList(cdefSkill: CDef.Skill): List<MessageType> {
            val list = mutableListOf<MessageType>()
            // 囁き
            if (cdefSkill.isViewableWerewolfSay) list.add(MessageType(CDef.MessageType.人狼の囁き))
            // 共鳴
            if (cdefSkill.isAvailableSympathizeSay) list.add(MessageType(CDef.MessageType.共鳴発言))

            return list
        }

        private fun getCountCamp(cdefSkill: CDef.Skill): Camp? {
            return when {
                cdefSkill.isCountWolf -> return Camp(CDef.Camp.人狼陣営)
                cdefSkill.isNoCount -> return null
                else -> Camp(CDef.Camp.村人陣営)
            }
        }
    }

    fun getAbilities(): AbilityTypes {
        val cdefSkill = CDef.Skill.codeOf(code) ?: return AbilityTypes(listOf())
        return Companion.getAbilities(cdefSkill)
    }

    fun toCdef(): CDef.Skill = CDef.Skill.codeOf(code)
}

fun CDef.Skill.toModel(): Skill = Skill(this)