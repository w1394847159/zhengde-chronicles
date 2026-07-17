package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.game.edict.EdictEffect
import com.zhengde.chronicles.game.world.WorldState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 叙事系统 — 把推演结果渲染成明清白话小说风格
 */
@Singleton
class NarrativeSystem @Inject constructor() {

    /**
     * 渲染推演结果叙事
     */
    fun render(
        effect: EdictEffect,
        previousState: WorldState,
        newState: WorldState,
        playerName: String = "朕"
    ): String {
        val sb = StringBuilder()

        // 开篇
        sb.appendLine("却说陛下")

        // 诏书效果叙事（优先用 LLM 的 narrative_summary）
        if (effect.summary.isNotBlank()) {
            sb.appendLine(effect.summary)
        }

        // 数值变化叙事
        val changes = describeChanges(effect, previousState, newState)
        if (changes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine(changes)
        }

        // 连锁反应
        if (effect.unexpectedConsequences.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("⚡ 连锁反应：${effect.unexpectedConsequences}")
        }

        // 收尾（正德特色）
        sb.appendLine()
        sb.appendLine("正是：${generateClosure(effect)}")

        return sb.toString()
    }

    /**
     * 数值变化 → 叙事描述
     */
    private fun describeChanges(effect: EdictEffect, prev: WorldState, next: WorldState): String {
        val lines = mutableListOf<String>()

        if (effect.treasuryDelta != 0) {
            val direction = if (effect.treasuryDelta > 0) "增加" else "减少"
            lines.add("国库${direction}银${kotlin.math.abs(effect.treasuryDelta)}万两（${prev.treasury}→${next.treasury}）")
        }
        if (effect.militaryDelta != 0) {
            val direction = if (effect.militaryDelta > 0) "提振" else "折损"
            lines.add("军力${direction}（${prev.military}→${next.military}）")
        }
        if (effect.prestigeDelta != 0) {
            val direction = if (effect.prestigeDelta > 0) "提升" else "受损"
            lines.add("威望${direction}（${prev.prestige}→${next.prestige}）")
        }
        if (effect.courtStabilityDelta != 0) {
            val direction = if (effect.courtStabilityDelta > 0) "改善" else "恶化"
            lines.add("朝纲${direction}（${prev.courtStability}→${next.courtStability}）")
        }
        if (effect.playfulnessDelta != 0) {
            val direction = if (effect.playfulnessDelta > 0) "渐涨" else "收敛"
            lines.add("玩心${direction}（${prev.playfulness}→${next.playfulness}）")
        }

        // 省份变化
        for ((province, delta) in effect.popularSupportDelta) {
            val direction = if (delta > 0) "稍安" else "浮动"
            val prevPs = prev.provinces[province]?.popularSupport ?: 50
            val nextPs = next.provinces[province]?.popularSupport ?: 50
            lines.add("${province}民心${direction}（${prevPs}→${nextPs}）")
        }

        // 派系变化
        for ((faction, delta) in effect.factionRelationDelta) {
            val direction = if (delta > 0) "缓和" else "紧张"
            lines.add("${faction}关系${direction}")
        }

        return lines.joinToString("\n") { "  · $it" }
    }

    /**
     * 生成收尾诗/对句
     */
    private fun generateClosure(effect: EdictEffect): String {
        return when {
            effect.treasuryDelta < -100 || effect.militaryDelta < -50 ->
                "兴，百姓苦；亡，百姓苦。"
            effect.prestigeDelta > 10 ->
                "天威所至，四海宾服。"
            effect.playfulnessDelta > 5 ->
                "人生得意须尽欢，莫使金樽空对月。"
            effect.courtStabilityDelta > 10 ->
                "君臣同心，其利断金。"
            effect.corruptionDelta > 5 ->
                "三年清知府，十万雪花银。"
            else ->
                "时势造英雄，英雄亦造时势。"
        }
    }
}
