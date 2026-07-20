package com.zhengde.chronicles.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhengde.chronicles.agent.LlmClient
import com.zhengde.chronicles.agent.LlmConfig
import com.zhengde.chronicles.agent.TokenTracker
import com.zhengde.chronicles.game.engine.*
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.Minister
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.ui.screens.DialogueMessage
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
    val tokenTracker: TokenTracker,
    private val ministerManager: MinisterManager
) : ViewModel() {

    // ========== 引擎状态 ==========
    private val _engineState = MutableStateFlow<EngineUiState>(EngineUiState.Idle)
    val engineState: StateFlow<EngineUiState> = _engineState.asStateFlow()

    private val _worldState = MutableStateFlow<WorldState?>(null)
    val worldState: StateFlow<WorldState?> = _worldState.asStateFlow()

    private val _narrative = MutableStateFlow<String?>(null)
    val narrative: StateFlow<String?> = _narrative.asStateFlow()

    private val _activeEvents = MutableStateFlow<List<ActiveEvent>>(emptyList())
    val activeEvents: StateFlow<List<ActiveEvent>> = _activeEvents.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========== 大臣对话状态 ==========
    private val _dialogueMessages = MutableStateFlow<List<DialogueMessage>>(emptyList())
    val dialogueMessages: StateFlow<List<DialogueMessage>> = _dialogueMessages.asStateFlow()

    private val _dialogueMinister = MutableStateFlow<Minister?>(null)
    val dialogueMinister: StateFlow<Minister?> = _dialogueMinister.asStateFlow()

    private val _isDialogueProcessing = MutableStateFlow(false)
    val isDialogueProcessing: StateFlow<Boolean> = _isDialogueProcessing.asStateFlow()

    // ========== API 配置 ==========
    private val _apiStatus = MutableStateFlow<String?>(null)
    val apiStatus: StateFlow<String?> = _apiStatus.asStateFlow()

    // ========== 游戏生命周期 ==========

    fun newGame() {
        val state = worldEngine.newGame(1505)
        _worldState.value = state
        _narrative.value = "正德元年五月，武宗皇帝登基。天下初定，然暗流涌动……"
        _activeEvents.value = emptyList()
        _engineState.value = EngineUiState.Ready(state.turn)
    }

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

                if (result.violations.isNotEmpty()) {
                    _error.value = "部分推演效果已自动校准（${result.violations.size} 项调整）"
                }
            } else {
                _engineState.value = EngineUiState.Error(result.error)
                _error.value = result.error
            }
        }
    }

    // ========== 大臣对话 ==========

    /** 打开与某大臣的对话 */
    fun startDialogue(minister: Minister) {
        _dialogueMinister.value = minister
        _dialogueMessages.value = emptyList()
        _isDialogueProcessing.value = false
    }

    /** 关闭对话 */
    fun closeDialogue() {
        _dialogueMinister.value = null
        _dialogueMessages.value = emptyList()
    }

    /** 向大臣发问 */
    fun sendToMinister(question: String) {
        val minister = _dialogueMinister.value ?: return
        val state = _worldState.value ?: return

        // 添加玩家消息
        _dialogueMessages.value = _dialogueMessages.value + listOf(
            DialogueMessage(text = question, isMinister = false, ministerName = "朕")
        )
        _isDialogueProcessing.value = true

        viewModelScope.launch {
            val prompt = buildMinisterPrompt(minister, question, state)
            val response = llmClient.generate(prompt)

            if (response != null) {
                val reply = extractMinisterReply(response)
                _dialogueMessages.value = _dialogueMessages.value + listOf(
                    DialogueMessage(text = reply, isMinister = true, ministerName = minister.name)
                )

                // 记录对话摘要到大臣状态
                val summary = "${minister.name}奏对：${question.take(20)}… → ${reply.take(30)}…"
                val newStates = ministerManager.updateLoyalty(state, minister.id, 2)
                _worldState.value = state.copy(ministerStates = newStates)
            } else {
                _dialogueMessages.value = _dialogueMessages.value + listOf(
                    DialogueMessage(
                        text = "……（${minister.name}沉吟不语，似乎不愿回答这个问题）",
                        isMinister = true,
                        ministerName = minister.name
                    )
                )
            }
            _isDialogueProcessing.value = false
        }
    }

    /** 构建大臣对话的 Prompt */
    private fun buildMinisterPrompt(minister: Minister, question: String, state: WorldState): String {
        val ministerContext = ministerManager.buildMinisterContext(state)

        return """
你正在扮演明朝正德年间的官员 ${minister.name}（${minister.title}）。

你的设定：
- 派系：${minister.faction}
- 性格：${minister.personality}
- 政治立场：${minister.stance}
- 简介：${minister.description}
- 三维能力：政治${minister.politics} 军事${minister.military} 智力${minister.intelligence}

对话要求：
1. 用第一人称回应，语言风格符合你的身份和时代背景
2. 基于你的派系立场给出建议，不要越出你的身份
3. 如果皇帝问的问题触及你的利益，你会委婉或直接地表达
4. 回应要简洁有力，控制在100字以内
5. 回答要有实质内容，不要空洞的场面话

当前朝局：
$ministerContext

皇帝问你：「${question}」

请用第一人称以${minister.name}的身份回答。
""".trimIndent()
    }

    /** 从 LLM 回复中提取大臣的回答 */
    private fun extractMinisterReply(response: String): String {
        // 去掉可能的引号和角色前缀
        return response
            .trim()
            .removePrefix("${_dialogueMinister.value?.name}：")
            .removePrefix("${_dialogueMinister.value?.name}：\"")
            .removePrefix("\"")
            .removeSuffix("\"")
            .trim()
    }

    // ========== API 配置 ==========

    fun updateApiConfig(config: LlmConfig) {
        llmClient.config = config
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _apiStatus.value = "测试中……"
            val result = llmClient.checkConnection()
            _apiStatus.value = if (result.success) "✅ 连接成功" else "❌ ${result.message}"
        }
    }

    fun clearError() {
        _error.value = null
        _apiStatus.value = null
    }

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
