package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.agent.LlmClient
import com.zhengde.chronicles.agent.PromptBuilder
import com.zhengde.chronicles.agent.TokenTracker
import com.zhengde.chronicles.game.memory.MemorySystem
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.WorldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 世界引擎主循环 — 游戏的心脏
 *
 * 管理整个回合推演流程：
 * 1. 接收玩家诏书
 * 2. 构建 Prompt → 调用 LLM
 * 3. 解析推演结果 → 校验合理性
 * 4. 更新状态 → 检测事件
 * 5. 压缩记忆
 * 6. 返回结果
 */
@Singleton
class WorldEngine @Inject constructor(
    private val stateManager: StateManager,
    private val effectSystem: EffectSystem,
    private val eventSystem: EventSystem,
    private val memorySystem: MemorySystem,
    private val narrativeSystem: NarrativeSystem,
    private val llmClient: LlmClient,
    private val promptBuilder: PromptBuilder,
    private val tokenTracker: TokenTracker,
    private val featureManager: FeatureManager
) {

    /** 引擎状态 */
    sealed class State {
        data object Idle : State()
        data object Processing : State()
        data class AwaitingEdict(val turn: Int) : State()
        data class Error(val reason: String, val recoverable: Boolean) : State()
    }

    private var _state: State = State.Idle
    val currentState: State get() = _state

    /**
     * 初始化新游戏
     */
    fun newGame(startYear: Int = 1505): WorldState {
        memorySystem.reset()
        val state = stateManager.initialize(startYear)
        _state = State.AwaitingEdict(0)
        return state
    }

    /**
     * 执行一个完整回合
     *
     * @param edictContent 玩家诏书内容
     * @return 推演结果
     */
    suspend fun executeTurn(edictContent: String): TurnResult = withContext(Dispatchers.IO) {
        _state = State.Processing

        try {
            val previousState = stateManager.currentState

            // ===== 1. 构建 Prompt =====
            val prompt = promptBuilder.buildEdictPrompt(edictContent, previousState)

            // ===== 2. 调用 LLM =====
            val rawResponse = llmClient.generate(prompt)
                ?: return@withContext TurnResult(
                    success = false,
                    error = "LLM 调用失败，请检查 API 配置和网络连接",
                    recoverable = true
                )

            tokenTracker.track(prompt, rawResponse)

            // ===== 3. 解析推演结果 =====
            when (val parseResult = effectSystem.parse(rawResponse, previousState)) {
                is ParseResult.Failed -> {
                    return@withContext TurnResult(
                        success = false,
                        error = parseResult.error,
                        recoverable = true
                    )
                }
                is ParseResult.Violated -> {
                    // 记录违规但继续执行（可配置为拒绝执行）
                    val effect = effectSystem.clampViolations(parseResult.effect)
                    val newState = stateManager.applyEffect(effect)

                    // ===== 4. 事件检测 =====
                    val events = eventSystem.detect(newState, previousState)
                    val featureEvents = featureManager.checkAllTriggers(newState)

                    // ===== 5. 记忆处理 =====
                    memorySystem.compress(
                        turn = newState.turn,
                        edictSummary = edictContent,
                        effectCausation = effect.causation,
                        newState = newState,
                        triggeredEvents = (events + featureEvents).map { it.id }
                    )

                    // ===== 6. 渲染叙事 =====
                    val narrative = narrativeSystem.render(effect, previousState, newState)

                    _state = State.AwaitingEdict(newState.turn)

                    return@withContext TurnResult(
                        success = true,
                        narrative = narrative,
                        effect = effect,
                        oldState = previousState,
                        newState = newState,
                        events = events + featureEvents,
                        violations = parseResult.violations,
                        tokenCost = tokenTracker.lastCost
                    )
                }
                is ParseResult.Success -> {
                    val effect = parseResult.effect
                    val newState = stateManager.applyEffect(effect)

                    // ===== 4. 事件检测 =====
                    val events = eventSystem.detect(newState, previousState)
                    val featureEvents = featureManager.checkAllTriggers(newState)

                    // ===== 5. 记忆处理 =====
                    memorySystem.compress(
                        turn = newState.turn,
                        edictSummary = edictContent,
                        effectCausation = effect.causation,
                        newState = newState,
                        triggeredEvents = (events + featureEvents).map { it.id }
                    )

                    // ===== 6. 渲染叙事 =====
                    val narrative = narrativeSystem.render(effect, previousState, newState)

                    _state = State.AwaitingEdict(newState.turn)

                    return@withContext TurnResult(
                        success = true,
                        narrative = narrative,
                        effect = effect,
                        oldState = previousState,
                        newState = newState,
                        events = events + featureEvents,
                        tokenCost = tokenTracker.lastCost
                    )
                }
            }
        } catch (e: Exception) {
            _state = State.Error(e.message ?: "未知错误", recoverable = true)
            return@withContext TurnResult(
                success = false,
                error = "推演异常: ${e.message}",
                recoverable = true
            )
        }
    }

    /**
     * 获取当前世界状态
     */
    fun getCurrentWorldState(): WorldState = stateManager.currentState

    /**
     * 回滚到指定回合
     */
    fun rollback(turn: Int): TurnRollbackResult {
        val state = stateManager.rollbackTo(turn)
        return TurnRollbackResult(
            success = state != null,
            state = state,
            message = if (state != null) "已回滚至第${turn}回合"
            else "回滚失败：找不到第${turn}回合的快照"
        )
    }
}

// ========== 结果类型 ==========

data class TurnResult(
    val success: Boolean,
    val narrative: String = "",
    val effect: com.zhengde.chronicles.game.edict.EdictEffect? = null,
    val oldState: WorldState? = null,
    val newState: WorldState? = null,
    val events: List<ActiveEvent> = emptyList(),
    val violations: List<ValidationViolation> = emptyList(),
    val tokenCost: com.zhengde.chronicles.agent.TokenCost? = null,
    val error: String = "",
    val recoverable: Boolean = true
)

data class TurnRollbackResult(
    val success: Boolean,
    val state: WorldState?,
    val message: String
)
