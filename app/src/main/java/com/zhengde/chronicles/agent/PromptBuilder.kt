package com.zhengde.chronicles.agent

import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.memory.MemorySystem
import com.zhengde.chronicles.game.engine.StateManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prompt 构建器
 *
 * 这是整个项目最核心的部分之一。
 * 负责将当前世界状态、历史记忆、玩家诏书组装成 LLM 可以理解的 Prompt。
 *
 * 结构：
 * 1. 系统指令（角色设定 + 输出约束）
 * 2. 当前世界状态（数值）
 * 3. 历史记忆（分层注入）
 * 4. 玩家诏书
 * 5. 输出格式要求
 */
@Singleton
class PromptBuilder @Inject constructor(
    private val memorySystem: MemorySystem,
    private val stateManager: StateManager
) {

    /**
     * 构建完整 Prompt
     */
    fun buildEdictPrompt(
        edictContent: String,
        currentState: WorldState = stateManager.currentState
    ): String {
        val sb = StringBuilder()

        // ========== 1. 系统指令 ==========
        sb.appendLine(buildSystemInstruction())
        sb.appendLine()

        // ========== 2. 当前世界状态 ==========
        sb.appendLine(buildStatePrompt(currentState))
        sb.appendLine()

        // ========== 3. 历史记忆 ==========
        sb.appendLine(memorySystem.buildMemoryPrompt(currentState))
        sb.appendLine()

        // ========== 4. 玩家诏书 ==========
        sb.appendLine("【玩家诏书】")
        sb.appendLine("陛下下旨：\"${edictContent}\"")
        sb.appendLine()

        // ========== 5. 输出格式要求 ==========
        sb.appendLine(buildOutputFormat())

        return sb.toString()
    }

    /**
     * 系统指令
     */
    private fun buildSystemInstruction(): String = """
你是一位精通明史的推演引擎，扮演《正德风云录》的世界模拟器。

你的职责：
1. 根据皇帝的诏书（自然语言），推演它对大明王朝各方面的影响
2. 输出必须是严格的 JSON 格式，不能包含其他文本
3. 所有数值变化必须合理，不能凭空产生或消失资源

世界设定：
- 你生活在明武宗朱厚照正德年间（公元1505-1521年）
- 朱厚照（玩家）性格跳脱不羁，热爱武事、豹房、微服出巡
- 正德朝的特点是：皇帝玩心重 + 八虎弄权 + 边患不断 + 宁王隐患
- 语言风格需符合明清白话小说质感

推演原则：
1. 历史合理性：诏书效果必须符合明朝中期的社会条件和生产力水平
2. 因果逻辑：任何数值变化必须有明确的因果解释
3. 蝴蝶效应：重大决策应有连锁反应
4. 角色一致性：大臣和派系的反应需符合各自立场（八虎趋利、文官守旧等）
5. 拒绝超自然：不允许天降祥瑞、修仙、超自然力量等
""".trimIndent()

    /**
     * 世界状态 Prompt（紧凑格式，省 Token）
     */
    private fun buildStatePrompt(state: WorldState): String {
        val sb = StringBuilder()
        sb.appendLine("【当前世界状态】")

        // 国势
        sb.appendLine("国库:${state.treasury}万两 | 粮储:${state.granary}万石 | 军力:${state.military}")
        sb.appendLine("边患:${state.borderThreat} | 朝纲:${state.courtStability} | 贪腐:${state.corruption}")

        // 皇帝
        sb.appendLine("皇帝——威望:${state.prestige} 精力:${state.energy} 玩心:${state.playfulness} 武艺:${state.martialSkill} 政治智慧:${state.politicalWisdom}")

        // 省份（紧凑）
        sb.appendLine("省份民心：")
        state.provinces.entries.joinTo(sb, separator = " | ") { (name, p) ->
            "$name:${p.popularSupport}"
        }

        // 派系
        sb.appendLine()
        sb.appendLine("派系关系：")
        state.factionRelations.entries.joinTo(sb, separator = " | ") { (name, r) ->
            val level = when {
                r >= 60 -> "亲善"
                r >= 30 -> "中立"
                r >= 10 -> "紧张"
                else -> "敌对"
            }
            "$name:${r}($level)"
        }

        // 活跃事件
        if (state.activeEvents.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("待处理事件：")
            state.activeEvents.forEach { e ->
                sb.appendLine("  · [${e.title}] ${e.description.take(50)}")
            }
        }

        return sb.toString()
    }

    /**
     * 输出格式约束
     */
    private fun buildOutputFormat(): String = """
【输出格式】
请严格按以下 JSON Schema 输出（不要加 markdown 代码块标记，纯 JSON）：

{
  "effect": {
    "narrative_summary": "叙事描述（明清白话，简述圣旨执行情况，50-150字）",
    "treasury_delta": 0,
    "granary_delta": 0,
    "military_delta": 0,
    "border_threat_delta": 0,
    "court_stability_delta": 0,
    "corruption_delta": 0,
    "prestige_delta": 0,
    "energy_delta": 0,
    "playfulness_delta": 0,
    "martial_skill_delta": 0,
    "political_wisdom_delta": 0,
    "popular_support": { "省份名": -5 },
    "faction_relation": { "八虎": -10, "内阁": 5 },
    "set_flags": [],
    "clear_flags": [],
    "causation": "因果说明（30字以内）",
    "unexpected_consequences": "意外连锁反应（可选）"
  }
}

约束：
- treasury_delta（国库变化）范围: -200 到 200
- military_delta（军力变化）范围: -150 到 150  
- 其他属性 delta 范围: -20 到 20
- faction_relation（派系关系）变化范围: -25 到 25
- popular_support（省份民心）变化范围: -15 到 15
- narrative_summary 不能为空
- 所有数值变化必须合理且有因果解释
""".trimIndent()
}
