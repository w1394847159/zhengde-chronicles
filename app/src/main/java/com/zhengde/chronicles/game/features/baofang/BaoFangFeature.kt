package com.zhengde.chronicles.game.features.baofang

import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.EventType
import javax.inject.Inject
import javax.inject.Singleton
import com.zhengde.chronicles.game.features.GameFeature
import com.zhengde.chronicles.game.features.FeatureResult

/**
 * 🐆 豹房系统 — 正德朝最具辨识度的玩法
 *
 * 豹房是朱厚照的私人天地。在这里他可以：
 * - 召八虎饮宴（亲近宦官，疏远朝政）
 * - 与边将夜谈（结交武人，准备亲征）
 * - 独自静养（恢复精力）
 * - 触发特殊事件（王阳明求见等）
 *
 * 核心机制：
 * - 消耗 1 回合进入豹房
 * - 在豹房内做任何事都会降朝纲
 * - 但会解锁特殊能力和情报
 * - 玩心越高，豹房收益越大、朝纲损失也越大
 */
@Singleton
class BaoFangFeature @Inject constructor() : GameFeature {

    override val name: String = "豹房"

    /** 豹房内可选行为 */
    enum class BaoFangAction(
        val label: String,
        val desc: String,
        val playfulnessDelta: Int,
        val courtDelta: Int,
        val energyDelta: Int,
        val prestigeDelta: Int = 0
    ) {
        EIGHT_TIGERS_BANQUET(
            "召八虎饮宴",
            "与刘瑾、马永成等八虎在豹房纵酒行乐，听他们讲宫外趣事",
            +8, -10, -5
        ),
        BORDER_GENERALS_TALK(
            "边将夜谈",
            "召江彬等边军将领密谈，了解边关军情，商讨用兵之道",
            +3, -5, -5, +3
        ),
        QUIET_RETREAT(
            "独自静养",
            "在豹房深处独处，弹琴读书，远离朝堂喧嚣",
            -5, 0, +15
        ),
        WANG_YANGMING_AUDIENCE(
            "王阳明求见（特殊）",
            "王守仁在豹房外求见，说有要事面陈",
            -3, +5, -3, +5
        ),
        FIGHTING_PRACTICE(
            "演武较技",
            "在豹房校场与边军将士比武，舒展筋骨",
            +2, -3, -8, +4
        );

        /** 根据玩心调整效果 */
        fun adjustByPlayfulness(baseDelta: Int, playfulness: Int): Int {
            val multiplier = when {
                playfulness >= 80 -> 1.5
                playfulness >= 60 -> 1.2
                playfulness >= 40 -> 1.0
                else -> 0.8
            }
            return (baseDelta * multiplier).toInt()
        }
    }

    override fun checkTrigger(state: WorldState): Boolean {
        // 豹房随时可以进入（消耗 1 回合）
        return state.energy >= 10
    }

    override fun onActivate(state: WorldState): List<ActiveEvent> {
        val event = ActiveEvent(
            id = "baofang_enter_${state.turn}",
            title = "陛下驾临豹房",
            description = "陛下移驾豹房。此处距紫禁城不过数里，却像是另一个天地。灯火通明，丝竹悦耳，八虎早已候驾。",
            type = EventType.NARRATIVE_DRIVEN,
            deadline = state.turn + 1,
            turnCreated = state.turn
        )
        return listOf(event)
    }

    /**
     * 执行豹房内的选择
     */
    override fun executeChoice(state: WorldState, choiceId: String): FeatureResult {
        val action = try {
            BaoFangAction.valueOf(choiceId)
        } catch (e: IllegalArgumentException) {
            return FeatureResult(
                narrative = "陛下在豹房踱步，未做决断。",
                stateChanges = mapOf("courtStability" to -2)
            )
        }

        val playfulness = state.playfulness
        val adjustedCourt = action.adjustByPlayfulness(action.courtDelta, playfulness)
        val adjustedPrestige = action.adjustByPlayfulness(action.prestigeDelta, playfulness)
        val adjustedEnergy = action.adjustByPlayfulness(action.energyDelta, playfulness)
        val adjustedPlay = action.adjustByPlayfulness(action.playfulnessDelta, playfulness)

        val narrative = buildNarrative(action, state)
        val changes = mutableMapOf(
            "courtStability" to adjustedCourt,
            "energy" to adjustedEnergy,
            "playfulness" to adjustedPlay
        )
        if (adjustedPrestige != 0) changes["prestige"] = adjustedPrestige
        if (action == BaoFangAction.QUIET_RETREAT) {
            changes["playfulness"] = -(Math.abs(adjustedPlay))
        }

        val flags = mutableListOf<String>()
        val newEvents = mutableListOf<ActiveEvent>()

        when (action) {
            BaoFangAction.BORDER_GENERALS_TALK -> {
                flags.add("unlocked_expedition")
            }
            BaoFangAction.WANG_YANGMING_AUDIENCE -> {
                flags.add("met_wangyangming")
                flags.add("milestone_wangyangming")
                changes["politicalWisdom"] = (changes["politicalWisdom"] ?: 0) + 8
            }
            BaoFangAction.FIGHTING_PRACTICE -> {
                changes["martialSkill"] = (changes["martialSkill"] ?: 0) + 4
            }
            else -> {}
        }

        return FeatureResult(
            stateChanges = changes,
            setFlags = flags,
            narrative = narrative,
            newEvents = newEvents
        )
    }

    /**
     * 获取当前可用的豹房行为列表
     */
    fun getAvailableActions(state: WorldState): List<BaoFangAction> {
        val actions = BaoFangAction.entries.toMutableList()

        // 王阳明求见需要特殊条件
        if (!state.hasFlag("met_wangyangming") && state.politicalWisdom < 30) {
            actions.remove(BaoFangAction.WANG_YANGMING_AUDIENCE)
        }

        // 精力过低时不能演武
        if (state.energy < 20) {
            actions.remove(BaoFangAction.FIGHTING_PRACTICE)
        }

        return actions
    }

    private fun buildNarrative(action: BaoFangAction, state: WorldState): String {
        val playLevel = when {
            state.playfulness >= 80 -> "陛下一进豹房便如鱼得水，兴致极高"
            state.playfulness >= 60 -> "陛下心情不错，面带笑意"
            state.playfulness >= 40 -> "陛下神色如常，看不出喜怒"
            else -> "陛下似乎有些心不在焉，心事重重"
        }

        return when (action) {
            BaoFangAction.EIGHT_TIGERS_BANQUET ->
                "却说$playLevel。刘瑾率八虎设宴，珍馐百味，歌舞升平。" +
                        "酒过三巡，刘瑾凑到御前低声道：" +
                        "'陛下，内阁那些老顽固又在背后嚼舌根了……'" +
                        "\n\n正是：朱门酒肉臭，路有冻死骨。"

            BaoFangAction.BORDER_GENERALS_TALK ->
                "是夜，$playLevel。江彬等边将卸甲入豹房，带来边关最新军报。" +
                        "他们详陈鞑靼动向，又献上蒙古战马数匹。" +
                        "陛下听得兴起，当场试马，尽兴而归。" +
                        "\n\n正是：男儿何不带吴钩，收取关山五十州。"

            BaoFangAction.QUIET_RETREAT ->
                "$playLevel。屏退左右，独坐竹榻。" +
                        "窗外虫鸣阵阵，案上檀香袅袅。" +
                        "这一刻，陛下不是皇帝，只是一个二十岁的年轻人。" +
                        "\n\n正是：偷得浮生半日闲。"

            BaoFangAction.WANG_YANGMING_AUDIENCE ->
                "$playLevel。王守仁入豹房，长揖不拜。" +
                        "他开门见山：「陛下可知，大明的江山不在紫禁城，而在百姓心中。」" +
                        "\n\n一番长谈，陛下若有所悟。" +
                        "正是：听君一席话，胜读十年书。"

            BaoFangAction.FIGHTING_PRACTICE ->
                "$playLevel。陛下脱去龙袍，换上一身劲装。" +
                        "与边军将士在校场你来我往，刀光剑影。" +
                        "虽为天子，手上功夫却也不弱，众将暗暗喝彩。" +
                        "\n\n正是：文能提笔安天下，武能上马定乾坤。"
        }
    }
}
