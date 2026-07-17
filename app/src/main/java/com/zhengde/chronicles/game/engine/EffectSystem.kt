package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.game.edict.EdictEffect
import com.zhengde.chronicles.game.world.WorldState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 效果解析与校验系统
 *
 * 职责：
 * 1. 解析 LLM 返回的 JSON → EdictEffect
 * 2. 校验数值合理性（硬约束）
 * 3. 确保不能凭空产生资源
 *
 * 这是介于 LLM 和游戏状态之间的"安全阀"。
 */
@Singleton
class EffectSystem @Inject constructor() {

    /** 单次变化最大绝对值 */
    companion object {
        const val MAX_SINGLE_DELTA = 200       // 国库最大单次变化
        const val MAX_ATTRIBUTE_DELTA = 20     // 属性单次变化
        const val MAX_FACTION_DELTA = 25       // 派系关系单次变化
        const val MAX_PROVINCE_DELTA = 15      // 省份民心单次变化
    }

    /**
     * 解析 LLM 输出的 JSON 字符串 → EdictEffect
     *
     * @param json LLM 返回的 JSON
     * @param currentState 当前状态（用于校验）
     * @return 解析结果
     */
    fun parse(json: String, currentState: WorldState): ParseResult {
        return try {
            // 使用 Gson 解析 JSON
            val effect = parseJson(json)
            // 校验合理性
            val violations = validate(effect, currentState)
            if (violations.isEmpty()) {
                ParseResult.Success(effect)
            } else {
                ParseResult.Violated(effect, violations)
            }
        } catch (e: Exception) {
            ParseResult.Failed("解析推演结果失败: ${e.message}")
        }
    }

    /**
     * 校验数值合理性
     *
     * 规则：
     * 1. 单次变化不能超过 MAX_SINGLE_DELTA
     * 2. 属性变化不能超过 MAX_ATTRIBUTE_DELTA
     * 3. 派系关系变化不能超过 MAX_FACTION_DELTA
     * 4. 资源变化必须有因果链（如国库减少需有理由）
     */
    fun validate(effect: EdictEffect, state: WorldState): List<ValidationViolation> {
        val violations = mutableListOf<ValidationViolation>()

        // 国库变化校验
        if (kotlin.math.abs(effect.treasuryDelta) > MAX_SINGLE_DELTA) {
            violations.add(ValidationViolation(
                field = "treasuryDelta",
                message = "国库单次变化 |${effect.treasuryDelta}| 超过上限 $MAX_SINGLE_DELTA"
            ))
        }

        // 军力变化校验
        if (kotlin.math.abs(effect.militaryDelta) > MAX_SINGLE_DELTA) {
            violations.add(ValidationViolation(
                field = "militaryDelta",
                message = "军力单次变化 |${effect.militaryDelta}| 超过上限 $MAX_SINGLE_DELTA"
            ))
        }

        // 皇帝属性变化校验
        val attrDeltas = listOf(
            effect.prestigeDelta to "prestigeDelta",
            effect.energyDelta to "energyDelta",
            effect.playfulnessDelta to "playfulnessDelta",
            effect.martialSkillDelta to "martialSkillDelta",
            effect.politicalWisdomDelta to "politicalWisdomDelta"
        )
        for ((delta, field) in attrDeltas) {
            if (kotlin.math.abs(delta) > MAX_ATTRIBUTE_DELTA) {
                violations.add(ValidationViolation(
                    field = field,
                    message = "${field} 变化 |$delta| 超过上限 $MAX_ATTRIBUTE_DELTA"
                ))
            }
        }

        // 派系关系变化校验
        for ((faction, delta) in effect.factionRelationDelta) {
            if (kotlin.math.abs(delta) > MAX_FACTION_DELTA) {
                violations.add(ValidationViolation(
                    field = "factionRelationDelta.$faction",
                    message = "$faction 关系单次变化 |$delta| 超过上限 $MAX_FACTION_DELTA"
                ))
            }
        }

        // 省份民心变化校验
        for ((province, delta) in effect.popularSupportDelta) {
            if (kotlin.math.abs(delta) > MAX_PROVINCE_DELTA) {
                violations.add(ValidationViolation(
                    field = "popularSupportDelta.$province",
                    message = "$province 民心单次变化 |$delta| 超过上限 $MAX_PROVINCE_DELTA"
                ))
            }
        }

        // 叙事摘要不能为空
        if (effect.summary.isBlank()) {
            violations.add(ValidationViolation(
                field = "summary",
                message = "推演叙事摘要不能为空"
            ))
        }

        return violations
    }

    /**
     * 将有违规的数值 clamp 到合法范围内，而不是拒绝整个效果
     */
    fun clampViolations(effect: EdictEffect): EdictEffect {
        return effect.copy(
            treasuryDelta = kotlin.math.abs(effect.treasuryDelta).coerceAtMost(MAX_SINGLE_DELTA) * sign(effect.treasuryDelta),
            granaryDelta = kotlin.math.abs(effect.granaryDelta).coerceAtMost(MAX_SINGLE_DELTA) * sign(effect.granaryDelta),
            militaryDelta = kotlin.math.abs(effect.militaryDelta).coerceAtMost(MAX_SINGLE_DELTA) * sign(effect.militaryDelta),
            prestigeDelta = kotlin.math.abs(effect.prestigeDelta).coerceAtMost(MAX_ATTRIBUTE_DELTA) * sign(effect.prestigeDelta),
            energyDelta = kotlin.math.abs(effect.energyDelta).coerceAtMost(MAX_ATTRIBUTE_DELTA) * sign(effect.energyDelta),
            playfulnessDelta = kotlin.math.abs(effect.playfulnessDelta).coerceAtMost(MAX_ATTRIBUTE_DELTA) * sign(effect.playfulnessDelta)
        )
    }

    private fun sign(value: Int): Int = when {
        value > 0 -> 1
        value < 0 -> -1
        else -> 0
    }

    // ========== 内部 ==========

    private fun parseJson(json: String): EdictEffect {
        // 去除 Markdown 代码块标记（LLM 可能会返回 ```json ... ```）
        val cleaned = json
            .replace(Regex("""^```(?:json)?\s*\n"""), "")
            .replace(Regex("""\n```\s*$"""), "")

        // 用 com.google.gson.Gson 解析
        val gson = com.google.gson.Gson()
        val jsonObject = gson.fromJson(cleaned, com.google.gson.JsonObject::class.java)

        // 提取 effect 对象（LLM 可能返回 { effect: { ... } } 或直接 { ... }）
        val effectObj = if (jsonObject.has("effect")) {
            jsonObject.getAsJsonObject("effect")
        } else {
            jsonObject
        }

        fun getInt(key: String, default: Int = 0): Int {
            return try {
                effectObj.get(key)?.asInt ?: default
            } catch (e: Exception) { default }
        }

        fun getString(key: String, default: String = ""): String {
            return try {
                effectObj.get(key)?.asString ?: default
            } catch (e: Exception) { default }
        }

        fun getStringMap(key: String): Map<String, Int> {
            return try {
                val obj = effectObj.getAsJsonObject(key) ?: return emptyMap()
                obj.entrySet().associate { (k, v) -> k to v.asInt }
            } catch (e: Exception) { emptyMap() }
        }

        fun getStringList(key: String): List<String> {
            return try {
                val arr = effectObj.getAsJsonArray(key) ?: return emptyList()
                arr.map { it.asString }
            } catch (e: Exception) { emptyList() }
        }

        return EdictEffect(
            summary = getString("narrative_summary"),
            treasuryDelta = getInt("treasury_delta"),
            granaryDelta = getInt("granary_delta"),
            militaryDelta = getInt("military_delta"),
            borderThreatDelta = getInt("border_threat_delta"),
            courtStabilityDelta = getInt("court_stability_delta"),
            corruptionDelta = getInt("corruption_delta"),
            prestigeDelta = getInt("prestige_delta"),
            energyDelta = getInt("energy_delta"),
            playfulnessDelta = getInt("playfulness_delta"),
            martialSkillDelta = getInt("martial_skill_delta"),
            politicalWisdomDelta = getInt("political_wisdom_delta"),
            popularSupportDelta = getStringMap("popular_support"),
            factionRelationDelta = getStringMap("faction_relation"),
            setFlags = getStringList("set_flags"),
            clearFlags = getStringList("clear_flags"),
            causation = getString("causation"),
            unexpectedConsequences = getString("unexpected_consequences")
        )
    }
}

// ========== 结果类型 ==========

sealed class ParseResult {
    data class Success(val effect: EdictEffect) : ParseResult()
    data class Violated(val effect: EdictEffect, val violations: List<ValidationViolation>) : ParseResult()
    data class Failed(val error: String) : ParseResult()
}

data class ValidationViolation(
    val field: String,
    val message: String
)
