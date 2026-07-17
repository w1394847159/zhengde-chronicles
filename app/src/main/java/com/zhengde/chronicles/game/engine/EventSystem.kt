package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.game.world.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 事件检测与生成系统
 *
 * 职责：
 * 1. 硬阈值检测（民心 < 20 → 民变警告）
 * 2. 剧烈变化检测（单次 drop > 30%）
 * 3. 派系关系临界检测
 * 4. 返回触发的事件列表
 */
@Singleton
class EventSystem @Inject constructor() {

    /** 阈值规则 */
    private val thresholds = listOf(
        ThresholdRule("民变", "popularSupport", 20, TriggerDirection.BELOW),
        ThresholdRule("边患告急", "borderThreat", 75, TriggerDirection.ABOVE),
        ThresholdRule("国库空虚", "treasury", 500, TriggerDirection.BELOW),
        ThresholdRule("朝纲崩坏", "courtStability", 25, TriggerDirection.BELOW),
        ThresholdRule("贪腐横行", "corruption", 85, TriggerDirection.ABOVE),
        ThresholdRule("粮储告罄", "granary", 300, TriggerDirection.BELOW),
        ThresholdRule("皇帝精力耗尽", "energy", 15, TriggerDirection.BELOW),
        ThresholdRule("玩心太盛", "playfulness", 85, TriggerDirection.ABOVE)
    )

    /**
     * 检测世界状态变化，返回触发的事件列表
     */
    fun detect(newState: WorldState, oldState: WorldState): List<ActiveEvent> {
        val events = mutableListOf<ActiveEvent>()
        val turn = newState.turn

        // 1. 国势维度阈值检测
        for (rule in thresholds) {
            val newValue = getNumericField(newState, rule.field)
            val oldValue = getNumericField(oldState, rule.field)

            if (rule.isTriggered(newValue, oldValue)) {
                events.add(ActiveEvent(
                    id = "${rule.name}_${turn}",
                    title = rule.name,
                    description = buildThresholdDesc(rule, newValue),
                    type = EventType.THRESHOLD_TRIGGERED,
                    deadline = turn + 3,
                    turnCreated = turn
                ))
            }
        }

        // 2. 省份级民心检测
        for ((name, province) in newState.provinces) {
            if (province.popularSupport < 20) {
                val oldProvince = oldState.provinces[name]
                if (oldProvince == null || oldProvince.popularSupport >= 20) {
                    events.add(ActiveEvent(
                        id = "民变_${name}_$turn",
                        title = "${name}民变",
                        description = "${name}民心降至${province.popularSupport}，流民聚众起事，地方告急！",
                        type = EventType.THRESHOLD_TRIGGERED,
                        deadline = turn + 2,
                        turnCreated = turn
                    ))
                }
            }
            // 灾害检测
            if (province.disasterRisk > 50) {
                // 高灾害风险省份触发
            }
        }

        // 3. 派系关系检测
        for ((faction, relation) in newState.factionRelations) {
            if (relation < 10) {
                val oldRelation = oldState.factionRelations[faction] ?: 50
                if (oldRelation >= 10) {
                    events.add(ActiveEvent(
                        id = "派系危机_${faction}_$turn",
                        title = "${faction}危机",
                        description = "$faction 关系降至 $relation，恐生变故！",
                        type = EventType.FACTION_CRISIS,
                        deadline = turn + 3,
                        turnCreated = turn
                    ))
                }
            }
        }

        // 4. 剧烈变化检测
        // 国库骤减超过 30%
        val treasuryDrop = oldState.treasury - newState.treasury
        if (oldState.treasury > 0 && treasuryDrop > oldState.treasury * 0.3) {
            events.add(ActiveEvent(
                id = "国库骤减_$turn",
                title = "国库骤减",
                description = "一夕之间国库减银${treasuryDrop}万两，户部惊骇！",
                type = EventType.NARRATIVE_DRIVEN,
                deadline = turn + 2,
                turnCreated = turn
            ))
        }

        // 军力骤降
        val militaryDrop = oldState.military - newState.military
        if (oldState.military > 0 && militaryDrop > oldState.military * 0.3) {
            events.add(ActiveEvent(
                id = "军力溃败_$turn",
                title = "军力大损",
                description = "军力锐减，边防告急！",
                type = EventType.NARRATIVE_DRIVEN,
                deadline = turn + 2,
                turnCreated = turn
            ))
        }

        return events
    }

    private fun getNumericField(state: WorldState, field: String): Int {
        return when (field) {
            "treasury" -> state.treasury
            "granary" -> state.granary
            "military" -> state.military
            "borderThreat" -> state.borderThreat
            "courtStability" -> state.courtStability
            "corruption" -> state.corruption
            "prestige" -> state.prestige
            "energy" -> state.energy
            "playfulness" -> state.playfulness
            "martialSkill" -> state.martialSkill
            "politicalWisdom" -> state.politicalWisdom
            "popularSupport" -> state.provinces.values.map { it.popularSupport }.average().toInt()
            else -> 0
        }
    }

    private fun buildThresholdDesc(rule: ThresholdRule, value: Int): String {
        return when (rule.name) {
            "民变" -> "民心降至 $value，百姓困苦，恐有民变之危。"
            "边患告急" -> "边患已达 $value，鞑靼随时可能大举南侵！"
            "国库空虚" -> "国库仅余 $value 万两，百官俸禄与军饷皆难支应。"
            "朝纲崩坏" -> "朝纲指数 $value，政令难出紫禁城。"
            "贪腐横行" -> "贪腐指数 $value，官吏上下其手，国将不国。"
            "粮储告罄" -> "粮储仅剩 $value 万石，若遇荒年，饥民遍野。"
            "皇帝精力耗尽" -> "陛下精力耗竭，恐难理朝政。"
            "玩心太盛" -> "陛下玩心大起，群臣忧心忡忡。"
            else -> "${rule.name} 异常（当前值 $value）。"
        }
    }
}

data class ThresholdRule(
    val name: String,
    val field: String,
    val threshold: Int,
    val direction: TriggerDirection
) {
    fun isTriggered(newValue: Int, oldValue: Int): Boolean {
        return when (direction) {
            TriggerDirection.ABOVE -> newValue >= threshold && oldValue < threshold
            TriggerDirection.BELOW -> newValue <= threshold && oldValue > threshold
        }
    }
}

enum class TriggerDirection { ABOVE, BELOW }
