package com.zhengde.chronicles.game.features.expedition

import com.zhengde.chronicles.game.features.GameFeature
import com.zhengde.chronicles.game.features.FeatureResult
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.EventType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⚔️ 亲征系统 — 朱厚照御驾亲征
 *
 * 正德朝最戏剧性的玩法。皇帝亲自上战场。
 * 触发条件：边患 > 70 或军力 > 400 且玩家主动选择
 *
 * 推演结果取决于：
 * - 边军战力 vs 蒙古军力
 * - 后勤补给（国库/粮储）
 * - 后方稳定（朝纲/民心）
 * - 皇帝武艺值
 */
@Singleton
class ExpeditionFeature @Inject constructor() : GameFeature {

    override val name: String = "亲征"

    enum class BattleOutcome {
        GREAT_VICTORY,      // 大胜
        SMALL_VICTORY,      // 小胜
        STALEMATE,          // 对峙
        MINOR_LOSS,         // 小败
        CRUSHING_DEFEAT,    // 大败
        WOUNDED             // 皇帝受伤
    }

    override fun checkTrigger(state: WorldState): Boolean {
        return state.borderThreat >= 70 || state.hasFlag("unlocked_expedition")
    }

    override fun onActivate(state: WorldState): List<ActiveEvent> {
        val threatLevel = when {
            state.borderThreat >= 85 -> "十万火急"
            state.borderThreat >= 75 -> "边关告急"
            else -> "鞑靼犯边"
        }

        return listOf(ActiveEvent(
            id = "expedition_trigger_${state.turn}",
            title = "【⚔️】$threatLevel",
            description = "边关传来急报：鞑靼骑兵大举南侵，已破关塞。" +
                    "朝堂震动，有大臣主张增兵固守，也有武将建议主动出击。" +
                    "\n\n陛下意欲何为？",
            type = EventType.HISTORICAL_CALLBACK,
            deadline = state.turn + 2,
            turnCreated = state.turn
        ))
    }

    /**
     * 推演战局
     */
    fun resolveBattle(state: WorldState, action: String): FeatureResult {
        // 综合战力评分
        val militaryScore = state.military
        val generalScore = if (state.hasFlag("jiangbin_commander")) 20 else 0
        val supplyScore = (state.treasury / 100).coerceIn(0, 30)
        val emperorScore = state.martialSkill / 2
        val stabilityScore = if (state.courtStability >= 40) 10 else -10

        val totalScore = militaryScore + generalScore + supplyScore + emperorScore + stabilityScore
        val roll = (0..40).random() - 20  // 随机浮动
        val finalScore = totalScore + roll

        val outcome = when {
            finalScore >= 360 -> BattleOutcome.GREAT_VICTORY
            finalScore >= 300 -> BattleOutcome.SMALL_VICTORY
            finalScore >= 250 -> BattleOutcome.STALEMATE
            finalScore >= 200 -> BattleOutcome.MINOR_LOSS
            action.contains("亲征") && finalScore < 150 && emperorScore < 20 -> BattleOutcome.WOUNDED
            else -> BattleOutcome.CRUSHING_DEFEAT
        }

        return when (outcome) {
            BattleOutcome.GREAT_VICTORY -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to 20, "military" to 30, "borderThreat" to -35,
                    "courtStability" to 5, "playfulness" to 8
                ),
                setFlags = listOf("milestone_great_victory"),
                narrative = "圣驾亲征，大获全胜！\n\n" +
                        "陛下亲率边军出塞，与鞑靼主力会战于塞外。" +
                        "三军将士见天子亲自督战，士气如虹。" +
                        "一战斩首数千级，鞑靼可汗仅以身免。" +
                        "\n\n此战之后，边患稍息，陛下的威望如日中天。",
                newEvents = listOf()
            )
            BattleOutcome.SMALL_VICTORY -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to 8, "military" to 15, "borderThreat" to -15,
                    "courtStability" to 3, "playfulness" to 5
                ),
                setFlags = listOf("milestone_small_victory"),
                narrative = "陛下亲征，小有斩获。\n\n" +
                        "边军与鞑靼交战数日，互有胜负。最终陛下督战得力，阵斩敌酋数人，鞑靼退兵。" +
                        "\n\n虽非大胜，却也足以振奋军心。"
            )
            BattleOutcome.STALEMATE -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to -2, "borderThreat" to -5,
                    "courtStability" to -3, "playfulness" to -3,
                    "treasury" to -80
                ),
                narrative = "战事陷入僵持。\n\n" +
                        "两军对垒月余，互有攻守，却谁也无法取得决定性胜利。" +
                        "国库日蹙，粮草转运艰难。朝中已有微词，认为陛下不该轻离京师。" +
                        "\n\n正是：凭君莫话封侯事，一将功成万骨枯。"
            )
            BattleOutcome.MINOR_LOSS -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to -8, "military" to -15, "borderThreat" to 5,
                    "courtStability" to -10, "playfulness" to -5,
                    "treasury" to -120
                ),
                narrative = "出战不利，损兵折将。\n\n" +
                        "陛下轻敌冒进，中了鞑靼人的诱敌之计。前锋三千人几乎全军覆没。" +
                        "幸得江彬率精骑接应，方保圣驾无虞。" +
                        "\n\n消息传回京师，朝野哗然。"
            )
            BattleOutcome.CRUSHING_DEFEAT -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to -25, "military" to -40, "borderThreat" to 20,
                    "courtStability" to -20, "playfulness" to -15,
                    "treasury" to -200
                ),
                narrative = "⛔ 大败！\n\n" +
                        "陛下亲征遭遇惨败。鞑靼铁骑冲垮了明军阵线，将士死伤无数。" +
                        "陛下在乱军中几乎被俘，幸得亲兵死战护卫，方退回关内。" +
                        "\n\n此一战，京师震动，百官恐慌。" +
                        "正是：塞上长城空自许，镜中衰鬓已先斑。"
            )
            BattleOutcome.WOUNDED -> FeatureResult(
                stateChanges = mapOf(
                    "prestige" to -10, "military" to -20, "borderThreat" to 15,
                    "courtStability" to -15, "energy" to -30, "playfulness" to -10
                ),
                setFlags = listOf("emperor_wounded"),
                narrative = "⚠️ 陛下受伤！\n\n" +
                        "乱军之中，陛下亲冒矢石，不料身中流矢。" +
                        "虽无性命之忧，但伤势不轻，需回京调养。" +
                        "\n\n此消息若传出去，恐怕朝堂会有一场风波。"
            )
        }
    }

    override fun executeChoice(state: WorldState, choiceId: String): FeatureResult {
        return resolveBattle(state, choiceId)
    }
}
