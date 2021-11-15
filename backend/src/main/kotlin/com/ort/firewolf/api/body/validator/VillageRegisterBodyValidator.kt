package com.ort.firewolf.api.body.validator

import com.ort.firewolf.api.body.VillageRegisterBody
import com.ort.firewolf.domain.model.skill.Skill
import com.ort.firewolf.fw.FirewolfDateUtil
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

@Component
class VillageRegisterBodyValidator : Validator {
    override fun supports(clazz: Class<*>): Boolean {
        return VillageRegisterBody::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        if (errors.hasErrors()) return

        val body = target as VillageRegisterBody

        // 開始日時
        if (body.setting!!.time!!.startDatetime!!.isBefore(FirewolfDateUtil.currentLocalDateTime())) {
            errors.reject("", "開始日時に過去日は指定できません")
            return
        }

        // 編成
        validateOrganization(body, errors)
    }

    private fun validateOrganization(body: VillageRegisterBody, errors: Errors) {
        val organizationList = body.setting!!.organization!!.organization!!.replace("\r\n", "\n").split("\n")
        val min = organizationList.map { it.length }.min() ?: 0
        val max = organizationList.map { it.length }.max() ?: 0
        // 歯抜け
        if ((min..max).any { personNum -> organizationList.none { it.length == personNum } }) {
            errors.reject("", "最小人数から定員までの全ての人数に対する編成が必要です")
            return
        }
        // 重複
        if ((min..max).any { personNum -> organizationList.count { it.length == personNum } > 1 }) {
            errors.reject("", "同一人数に対する編成が複数存在しています")
            return
        }
        // 存在しない役職がいる
        if (organizationList.any { org ->
                org.toCharArray().any { Skill.skillByShortName(it.toString()) == null }
            }) {
            errors.reject("", "存在しない役職があります")
            return
        }

        // 役欠けありだが噛まれることができる役職が存在しない
        val isAvailableDummySkill = body.setting.rule!!.availableDummySkill!!
        if (isAvailableDummySkill
            && organizationList.any { org ->
                org.toCharArray().map { it.toString() }.none {
                    val cdefSkill = Skill.skillByShortName(it)!!.toCdef()
                    !cdefSkill.isNoDeadByAttack
                            && !cdefSkill.isNotSelectableAttack
                            && !cdefSkill.isForceDoubleSuicide
                }
            }
        ) {
            errors.reject("", "役欠けありの場合噛まれて死亡する役職を含めてください")
            return
        }

        // 人狼がいない
        if (organizationList.any { org ->
                org.toCharArray().map { it.toString() }
                    .none { Skill.skillByShortName(it)!!.toCdef().isHasAttackAbility }
            }) {
            errors.reject("", "襲撃役職が必要です")
            return
        }

        // 人狼が半数以上
        if (organizationList.any { org ->
                val personCount = org.length
                val wolfCount = org.toCharArray().map { it.toString() }
                    .count { Skill.skillByShortName(it)!!.toCdef().isHasAttackAbility }
                wolfCount >= personCount / 2
            }
        ) {
            errors.reject("", "襲撃役職は半分以下にしてください")
            return
        }
    }
}
