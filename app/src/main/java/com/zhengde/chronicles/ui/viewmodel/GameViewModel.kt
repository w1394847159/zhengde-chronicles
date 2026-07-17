package com.zhengde.chronicles.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhengde.chronicles.agent.LlmClient
import com.zhengde.chronicles.agent.LlmConfig
import com.zhengde.chronicles.agent.TokenTracker
import com.zhengde.chronicles.game.engine.*
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.WorldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 游戏主 ViewModel — UI 和引擎的桥梁
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val worldEngine: WorldEngine,
    private val llmClient: LlmClient,
    val tokenTracker: TokenTracker
) : ViewModel() {

    // ========== UI 状态 ==========

    /** 引擎状态 */
    private val _engineState = MutableStateFlow<EngineUiState>(EngineUiState.Idle)
    val engineState: StateFlow<EngineUiState> = _engineState.asStateFlow()

    /** 世界状态 */
    private val _worldState = MutableStateFlow<WorldState?>(null)
    val worldState: StateFlow<WorldState?> = _worldState.asStateFlow()

    /** 推演结果叙事 */
    private val _narrative = MutableStateFlow<String?>(null)
    val narrative: StateFlow<String?> = _narrative.asStateFlow()

    /** 当前事件列表 */
    private val _activeEvents = MutableStateFlow<List<ActiveEvent>>(emptyList())
    val activeEvents: StateFlow<List<ActiveEvent>> = _activeEvents.asStateFlow()

    /** 错误信息 */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========== 游戏生命周期 ==========

    /**
     * 开始新游戏
     */
    fun newGame() {
        val state = worldEngine.newGame(1505)
        _worldState.value = state
        _narrative.value = "正德元年五月，武宗皇帝登基。天下初定，然暗流涌动……"
        _activeEvents.value = emptyList()
        _engineState.value = EngineUiState.Ready(state.turn)
    }

    /**
     * 执行回合
     */
    fun executeTurn(edictContent: String) {
        if (edictContent.isBlank()) {
            _error.value = "诏书不可为空"
            return
        }

        _engineState.value = EngineUiState.Processing
        _error.value = null

        viewModelScope.launch {
            val result = worldEngine.executeTurn(edictContent)

            if (result.success) {
                _worldState.value = result.newState
                _narrative.value = result.narrative
                _activeEvents.value = result.events
                _engineState.value = EngineUiState.Ready(result.newState?.turn ?: 0)

                // 如果有数值违规但被 clamp 了，告知用户
                if (result.violations.isNotEmpty()) {
                    _error.value = "部分推演效果已自动校准（${result.violations.size} 项调整）"
                }
            } else {
                _engineState.value = EngineUiState.Error(result.error)
                _error.value = result.error
            }
        }
    }

    /**
     * 更新 API 配置
     */
    fun updateApiConfig(config: LlmConfig) {
        llmClient.config = config
    }

    /**
     * 测试 API 连接
     */
    fun testApiConnection() {
        viewModelScope.launch {
            _engineState.value = EngineUiState.Processing
            val result = llmClient.checkConnection()
            _error.value = if (result.success) null else result.message
            _engineState.value = EngineUiState.Ready(worldEngine.getCurrentWorldState().turn)
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 清除叙事（为下一轮准备）
     */
    fun clearNarrative() {
        _narrative.value = null
    }
}

/** 引擎 UI 状态 */
sealed class EngineUiState {
    data object Idle : EngineUiState()
    data object Processing : EngineUiState()
    data class Ready(val turn: Int) : EngineUiState()
    data class Error(val message: String) : EngineUiState()
}
