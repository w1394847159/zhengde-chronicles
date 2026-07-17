package com.zhengde.chronicles.game.features.wangyangming

import com.zhengde.chronicles.game.features.GameFeature
import com.zhengde.chronicles.game.features.FeatureResult
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.EventType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📜 王阳明事件链 — 正德朝最关键的剧情线
 *
 * 王守仁（王阳明）是正德朝最重要的历史人物。
 * 他的出场和命运取决于玩家的决策。
 *
 * 事件链阶段：
 * 1. 出场：王阳明上疏言事 → 被贬龙场 → 悟道
 * 2. 南赣巡抚：平定盗匪 → 练兵
 * 3. 宁王反叛：取决于江西民心 + 宗室关系 → 宁王是否造反
 * 4. 平叛：王阳明是否被启用 → 决定平叛难度和结局
 * 5. 心学崛起：王阳明的地位取决于他的功绩
 */
@Singleton
class WangYangMingFeature @Inject constructor() : GameFeature {

    override val name: String = "王阳明事件链"

    /** 事件链阶段 */
    enum class WyStage {
        HIDDEN,          // 未出场
        APPEARED,        // 已出场（上疏言事）
        LONGFIELD,       // 贬谪龙场
        ENLIGHTENED,     // 龙场悟道
        SOUTH_MINISTRY,  // 南赣巡抚
        NING_WAR,        // 宁王叛乱中
        HERO,            // 平叛功臣
        RECLUSE           // 归隐/被压制
    }

    override fun checkTrigger(state: WorldState): Boolean {
        // 王阳明事件链在多个阶段都可能触发
        val stage = getCurrentStage(state)
        return stage != WyStage.RECLUSE && stage != WyStage.HERO
    }

    override fun onActivate(state: WorldState): List<ActiveEvent> {
        val stage = getCurrentStage(state)

        return when (stage) {
            WyStage.HIDDEN -> {
                // 首次出场：王阳明上疏言事
                listOf(ActiveEvent(
                    id = "wy_first_appear_${state.turn}",
                    title = "【📜】王阳明上疏",
                    description = "兵部主事王守仁上书陛下，陈言边务八事。" +
                            "其言切中时弊，文辞犀利。" +
                            "奏疏末尾写道："陛下若不改弦更张，恐有肘腋之患。"",
                    type = EventType.NARRATIVE_DRIVEN,
                    deadline = state.turn + 2,
                    turnCreated = state.turn
                ))
            }
            WyStage.APPEARED -> {
                // 被贬龙场
                listOf(ActiveEvent(
                    id = "wy_longfield_${state.turn}",
                    title = "【📜】王阳明被贬",
                    description = "王守仁因得罪刘瑾，被廷杖四十，贬为贵州龙场驿丞。",
                    type = EventType.NARRATIVE_DRIVEN,
                    deadline = state.turn + 1,
                    turnCreated = state.turn
                ))
            }
            WyStage.LONGFIELD -> {
                // 龙场悟道
                listOf(ActiveEvent(
                    id = "wy_enlightenment_${state.turn}",
                    title = "【📜】龙场悟道",
                    description = "王守仁在龙场日夜静思，一日夜中忽有所悟："圣人之道，吾性自足。"",
                    type = EventType.NARRATIVE_DRIVEN,
                    deadline = state.turn + 1,
                    turnCreated = state.turn
                ))
            }
            WyStage.ENLIGHTENED -> {
                // 起复南赣
                val needGarrison = state.provinces.values.any { it.stability < 40 }
                if (needGarrison) {
                    listOf(ActiveEvent(
                        id = "wy_south_${state.turn}",
                        title = "【📜】南赣匪患",
                        description = "南赣一带盗匪横行，地方告急。" +
                                "有大臣推荐起复王守仁，任南赣巡抚，剿匪安民。",
                        type = EventType.NARRATIVE_DRIVEN,
                        deadline = state.turn + 3,
                        turnCreated = state.turn
                    ))
                } else {
                    emptyList()
                }
            }
            WyStage.SOUTH_MINISTRY -> {
                // 监听宁王动向
                if (state.factionRelation("宗室") < 40) {
                    listOf(ActiveEvent(
                        id = "wy_ning_warning_${state.turn}",
                        title = "【📜】王阳明密报",
                        description = "王守仁从江西传来密报："宁王朱宸濠近日频繁宴请地方官将，似有异动。"",
                        type = EventType.NARRATIVE_DRIVEN,
                        deadline = state.turn + 5,
                        turnCreated = state.turn
                    ))
                } else {
                    emptyList()
                }
            }
            WyStage.NING_WAR -> {
                // 宁王已反，是否启用王阳明平叛
                listOf(ActiveEvent(
                    id = "wy_ning_battle_${state.turn}",
                    title = "【⚔️】宁王反了！",
                    description = "宁王朱宸濠在江西起兵造反，连下数城。" +
                            "朝堂震动，陛下急召群臣议事。" +
                            "\n\n兵部尚书推荐：速起用王守仁，率兵平叛！",
                    type = EventType.HISTORICAL_CALLBACK,
                    deadline = state.turn + 2,
                    turnCreated = state.turn
                ))
            }
            else -> emptyList()
        }
    }

    override fun executeChoice(state: WorldState, choiceId: String): FeatureResult {
        val stage = getCurrentStage(state)

        return when (choiceId) {
            "wy_promote" -> promoteWang(state, stage)
            "wy_ignore" -> ignoreWang(state, stage)
            "wy_punish" -> punishWang(state, stage)
            else -> FeatureResult(
                narrative = "王守仁的奏疏被留中不发。他叹了一声，继续回衙门当值。"
            )
        }
    }

    private fun promoteWang(state: WorldState, stage: WyStage): FeatureResult {
        return when (stage) {
            WyStage.HIDDEN -> FeatureResult(
                stateChanges = mapOf("prestige" to 3, "politicalWisdom" to 3, "心学" to 5),
                setFlags = listOf("wy_appeared", "promoted_wang"),
                narrative = "陛下御览王守仁的奏疏，连连点头："这人有点意思。"\n\n" +
                        "当即下旨，擢升王守仁为都察院左佥都御史。" +
                        "消息传出，朝中有识之士无不欣慰。"
            )
            WyStage.SOUTH_MINISTRY -> FeatureResult(
                stateChanges = mapOf("心学" to 8, "prestige" to 5, "江西" to 5),
                setFlags = listOf("wy_south_commissioned", "milestone_wang_south"),
                narrative = "陛下下旨：命王守仁为南赣巡抚，剿抚并用，平定匪患。\n\n" +
                        "王守仁到任后，整肃军纪，训练乡勇，推行十家牌法。" +
                        "不到半年，南赣匪患悉平。"
            )
            WyStage.NING_WAR -> {
                // 启用王阳明平叛
                val wyPower = if (state.hasFlag("wy_south_commissioned")) 90 else 60
                val successRoll = (0..99).random()
                val success = successRoll < wyPower

                if (success) {
                    FeatureResult(
                        stateChanges = mapOf(
                            "prestige" to 15,
                            "courtStability" to 10,
                            "心学" to 15,
                            "宗室" to 10,
                            "江西" to 10
                        ),
                        setFlags = listOf("milestone_ning_rebels_defeated", "wy_hero"),
                        narrative = "王阳明率兵平叛，用兵如神！\n\n" +
                                "他先用离间计使宁王犹豫不决，延误战机。" +
                                "随后集中兵力，奇袭宁王老巢南昌。" +
                                "宁王回师救援，被王阳明以火攻大破之。" +
                                "前后仅三十五天，宁王之乱平定！" +
                                "\n\n正是：破山中贼易，破心中贼难。"
                    )
                } else {
                    FeatureResult(
                        stateChanges = mapOf(
                            "prestige" to -5,
                            "courtStability" to -8,
                            "宗室" to -10,
                            "江西" to -15,
                            "borderThreat" to 10
                        ),
                        setFlags = listOf("milestone_ning_rebels_victory_hard"),
                        narrative = "王守仁虽尽力平叛，但宁王势大，战事胶着。\n\n" +
                                "朝廷不得不从九边抽调精兵南下，才勉强将宁王之乱镇压下去。" +
                                "虽最终获胜，却消耗了大量国力。"
                    )
                }
            }
            else -> FeatureResult(narrative = "陛下暂时没有理会此事。")
        }
    }

    private fun ignoreWang(state: WorldState, stage: WyStage): FeatureResult {
        return when (stage) {
            WyStage.HIDDEN -> FeatureResult(
                stateChanges = mapOf("心学" to -2),
                setFlags = listOf("wy_ignored_first"),
                narrative = "王守仁的奏疏被丢在了一旁。" +
                        "陛下此时没心思管什么边务八事——豹房还等着呢。",
                newEvents = listOf(ActiveEvent(
                    id = "wy_resent_${state.turn}",
                    title = "王守仁心寒",
                    description = "王守仁见自己的奏疏如石沉大海，心灰意冷。",
                    type = EventType.NARRATIVE_DRIVEN,
                    deadline = state.turn + 5,
                    turnCreated = state.turn
                ))
            )
            WyStage.NING_WAR -> FeatureResult(
                stateChanges = mapOf("courtStability" to -10, "prestige" to -8),
                narrative = "陛下没有采纳起用王守仁的建议。\n\n" +
                        "平叛大任交给了其他将领。虽然最终也平定了宁王之乱，" +
                        "但耗费的钱粮和时间数倍于王阳明方案。" +
                        "战后论功，陛下想起了王守仁——但最好的时机已经过了。"
            )
            else -> FeatureResult(narrative = "暂且搁置。")
        }
    }

    private fun punishWang(state: WorldState, stage: WyStage): FeatureResult {
        return FeatureResult(
            stateChanges = mapOf("prestige" to -5, "courtStability" to -5, "心学" to -10),
            setFlags = listOf("wy_punished"),
            narrative = "陛下龙颜大怒："区区兵部主事，也敢妄议朝政？"\n\n" +
                    "下旨将王守仁廷杖四十，贬为贵州龙场驿丞。" +
                    "满朝文武，噤若寒蝉。" +
                    "\n\n只是——这一顿廷杖，会不会打出个了不得的人物来？"
        )
    }

    /**
     * 获取当前王阳明事件链的阶段
     */
    fun getCurrentStage(state: WorldState): WyStage {
        return when {
            state.hasFlag("wy_hero") -> WyStage.HERO
            state.hasFlag("wy_punished") -> WyStage.LONGFIELD
            state.hasFlag("milestone_ning_rebels_defeated") -> WyStage.HERO
            state.hasFlag("ning_rebellion_active") -> WyStage.NING_WAR
            state.hasFlag("wy_south_commissioned") -> WyStage.SOUTH_MINISTRY
            state.hasFlag("wy_enlightened") -> WyStage.ENLIGHTENED
            state.hasFlag("wy_longfield") -> WyStage.LONGFIELD
            state.hasFlag("wy_appeared") -> WyStage.APPEARED
            else -> WyStage.HIDDEN
        }
    }

    /**
     * 检测宁王是否应该造反
     */
    fun checkNingRebellion(state: WorldState): Boolean {
        if (state.hasFlag("ning_rebellion_active") || state.hasFlag("milestone_ning_rebels_defeated")) {
            return false
        }
        val jiangxiSupport = state.popularSupport("江西")
        val zongshiRelation = state.factionRelation("宗室")
        val courtStability = state.courtStability

        return jiangxiSupport < 35 && zongshiRelation < 30 && courtStability < 45
    }
}
