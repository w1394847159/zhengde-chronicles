package com.zhengde.chronicles.game.features.eighttigers

import com.zhengde.chronicles.game.engine.GameFeature
import com.zhengde.chronicles.game.engine.FeatureResult
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.EventType

/**
 * 🗡 八虎博弈系统 — 正德初年最核心的朝堂博弈
 *
 * 八虎（刘瑾为首）是朱厚照做太子时的玩伴，
 * 即位后权倾朝野。玩家需要决定：
 * - 杀刘瑾（需要串通张永，搞内部分化）
 * - 保刘瑾（继续让八虎掌权，贪腐加剧）
 * - 玩平衡（两边都给甜头，但治标不治本）
 *
 * 这个系统贯穿整个游戏，不是一次性事件。
 */
class EightTigersFeature constructor() : GameFeature {

    override val name: String = "八虎博弈"

    /** 八虎博弈的阶段 */
    enum class TigerPhase {
        EARLY,          // 开局：刘瑾一手遮天
        POWER_STRUGGLE, // 中期：权力斗争白热化
        RESOLUTION      // 结局：或杀或留
    }

    override fun checkTrigger(state: WorldState): Boolean {
        // 八虎关系 < 30 或 内阁关系 < 20 时触发
        val tigerRelation = state.factionRelation("八虎")
        val cabinetRelation = state.factionRelation("内阁")
        return tigerRelation < 35 || cabinetRelation < 25 ||
                state.corruption >= 75
    }

    override fun onActivate(state: WorldState): List<ActiveEvent> {
        val tigerRelation = state.factionRelation("八虎")
        val cabinetRelation = state.factionRelation("内阁")

        val title = when {
            state.corruption >= 80 -> "贪腐横行！刘瑾卖官鬻爵"
            tigerRelation < 30 -> "八虎不安，刘瑾疑惧"
            cabinetRelation < 20 -> "内阁密议：请诛刘瑾"
            else -> "朝堂暗流涌动"
        }

        return listOf(ActiveEvent(
            id = "tigers_${state.turn}",
            title = "【🗡】$title",
            description = buildSituationDesc(state),
            type = EventType.FACTION_CRISIS,
            deadline = state.turn + 4,
            turnCreated = state.turn
        ))
    }

    override fun executeChoice(state: WorldState, choiceId: String): FeatureResult {
        return when (choiceId) {
            "kill_liujin" -> executeKillLiuJin(state)
            "protect_liujin" -> executeProtectLiuJin(state)
            "balance" -> executeBalance(state)
            "promote_zhangyong" -> executePromoteZhangYong(state)
            else -> FeatureResult(
                narrative = "陛下沉吟不语，未作决断。",
                stateChanges = mapOf("courtStability" to -2, "八虎" to 2)
            )
        }
    }

    /**
     * 杀刘瑾
     * - 需要串通张永（八虎二号人物，与刘瑾有矛盾）
     * - 成功后威望暴涨，贪腐下降，但八虎关系大减
     * - 失败则八虎反扑，朝纲崩溃
     */
    private fun executeKillLiuJin(state: WorldState): FeatureResult {
        val hasZhangYong = state.hasFlag("allied_with_zhangyong")
        val successChance = if (hasZhangYong) 75 else 40
        val roll = (0..99).random()

        return if (roll < successChance) {
            // 成功
            FeatureResult(
                stateChanges = mapOf(
                    "prestige" to 15,
                    "corruption" to -20,
                    "courtStability" to 10,
                    "八虎" to -30,
                    "内阁" to 15,
                    "treasury" to 80
                ),
                setFlags = listOf("milestone_killed_liujin", "liujin_dead"),
                clearFlags = listOf("allied_with_zhangyong"),
                narrative = if (hasZhangYong) {
                    "陛下定计，密召张永入宫。\n\n" +
                            "张永早与刘瑾有隙，闻陛下欲除之，大喜，愿为内应。" +
                            "次日朝会，张永当众揭发刘瑾十七条大罪。" +
                            "陛下龙颜大怒，当场下令拿下刘瑾！\n\n" +
                            "刘瑾伏诛，抄家得银数百万两，满朝文武无不拍手称快。" +
                            "正是：善恶终有报，天道好轮回。"
                } else {
                    "陛下突然下旨，以谋反罪名将刘瑾下狱。\n\n" +
                            "刘瑾党羽虽试图反抗，但锦衣卫早有准备。" +
                            "三司会审，刘瑾认罪伏法。抄没家产，充盈国库。\n\n" +
                            "只是——八虎中其余几人，从此对陛下多了几分畏惧和疏远。"
                }
            )
        } else {
            // 失败
            FeatureResult(
                stateChanges = mapOf(
                    "prestige" to -10,
                    "corruption" to 5,
                    "courtStability" to -20,
                    "八虎" to -15,
                    "内阁" to -5
                ),
                setFlags = listOf("killing_liujin_failed"),
                narrative = "大事不妙！\n\n" +
                        "陛下欲除刘瑾的消息走漏了风声。" +
                        "刘瑾先发制人，领着八虎跪在陛下面前痛哭流涕，指天誓日表忠心。" +
                        "太后也出面说情……陛下只好暂缓处置。" +
                        "\n\n经此一役，刘瑾更加谨慎，八虎与陛下的关系也微妙了起来。"
            )
        }
    }

    /**
     * 保刘瑾
     */
    private fun executeProtectLiuJin(state: WorldState): FeatureResult {
        return FeatureResult(
            stateChanges = mapOf(
                "八虎" to 10,
                "内阁" to -15,
                "corruption" to 8,
                "courtStability" to -5,
                "prestige" to -3
            ),
            narrative = "陛下力保刘瑾。\n\n" +
                    "面对群臣弹劾，陛下不以为然：「刘瑾侍朕多年，不过贪财好利，何至于诛？」\n\n" +
                    "刘瑾感激涕零，更加卖力地讨陛下欢心。" +
                    "只是——朝中文武，心凉了半截。"
        )
    }

    /**
     * 平衡：两边都不偏袒
     */
    private fun executeBalance(state: WorldState): FeatureResult {
        return FeatureResult(
            stateChanges = mapOf(
                "内阁" to 3,
                "八虎" to 3,
                "courtStability" to 5,
                "corruption" to 3
            ),
            narrative = "陛下两边各打五十大板。\n\n" +
                    "一方面申斥刘瑾，命其收敛；另一方面也告诫内阁，不得结党营私。" +
                    "各打五十大板，看似公允。但——贪腐的根子没除，早晚还要出事。",
            newEvents = listOf(ActiveEvent(
                id = "corruption_looming_${state.turn}",
                title = "暗流未平",
                description = "贪腐仍在暗中蔓延，这只是暂时的平静。",
                type = EventType.NARRATIVE_DRIVEN,
                deadline = state.turn + 10,
                turnCreated = state.turn
            ))
        )
    }

    /**
     * 联合张永（内部分化）
     */
    private fun executePromoteZhangYong(state: WorldState): FeatureResult {
        return FeatureResult(
            stateChanges = mapOf(
                "八虎" to -5,
                "courtStability" to 3,
                "prestige" to 5
            ),
            setFlags = listOf("allied_with_zhangyong"),
            narrative = "陛下密召张永入豹房。\n\n" +
                    "张永此人虽是八虎之一，但与刘瑾素来不和。" +
                    "陛下以心腹之言相托，张永受宠若惊，表示愿为陛下效死。" +
                    "\n\n陛下手上多了一颗棋子。只是——这张永，真的可信么？"
        )
    }

    private fun buildSituationDesc(state: WorldState): String {
        val sb = StringBuilder()
        val tigerRelation = state.factionRelation("八虎")
        val cabinetRelation = state.factionRelation("内阁")

        sb.appendLine("刘瑾掌司礼监以来，权倾朝野，卖官鬻爵。")
        sb.appendLine()
        when {
            tigerRelation >= 70 -> sb.appendLine("八虎对陛下仍然忠心，至少表面如此。")
            tigerRelation >= 40 -> sb.appendLine("八虎对陛下有所怨言，但尚不敢放肆。")
            else -> sb.appendLine("八虎人心惶惶，刘瑾日夜不安。")
        }
        when {
            cabinetRelation >= 50 -> sb.appendLine("内阁与陛下同心，力主改革。")
            cabinetRelation >= 30 -> sb.appendLine("内阁对陛下有所不满，常上疏劝谏。")
            else -> sb.appendLine("内阁对陛下失望至极，私下联络言官，准备死谏。")
        }
        sb.appendLine()
        sb.appendLine("陛下可以：")
        sb.appendLine("  [诛] 借机除掉刘瑾（需联合张永）")
        sb.appendLine("  [保] 力保刘瑾，维持现状")
        sb.appendLine("  [衡] 两边各打五十大板")
        sb.appendLine("  [间] 拉拢张永，分化八虎（先做准备）")

        return sb.toString()
    }
}
