package com.zhengde.chronicles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhengde.chronicles.agent.LlmConfig
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.ui.viewmodel.EngineUiState
import com.zhengde.chronicles.ui.viewmodel.GameViewModel

/**
 * 御书房主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val engineState by viewModel.engineState.collectAsState()
    val worldState by viewModel.worldState.collectAsState()
    val narrative by viewModel.narrative.collectAsState()
    val activeEvents by viewModel.activeEvents.collectAsState()
    val error by viewModel.error.collectAsState()

    // 诏书输入
    var edictText by remember { mutableStateOf("") }

    // API 设置弹窗
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("https://api.deepseek.com/chat/completions") }
    var modelName by remember { mutableStateOf("deepseek-chat") }

    // 开局自动初始化
    LaunchedEffect(Unit) {
        if (engineState is EngineUiState.Idle) {
            viewModel.newGame()
        }
    }

    // 设置弹窗
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("API 配置", color = MaterialTheme.colorScheme.primary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = apiUrl,
                        onValueChange = { apiUrl = it },
                        label = { Text("API Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("模型名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.updateApiConfig(LlmConfig(
                                apiBaseUrl = apiUrl,
                                apiKey = apiKey,
                                model = modelName
                            ))
                            viewModel.testApiConnection()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("测试连接")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("完成")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "正德风云录",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { showSettings = true }) {
                        Text("API", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== 引擎状态 =====
            when (engineState) {
                is EngineUiState.Processing -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "圣旨推演中……",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is EngineUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = (engineState as EngineUiState.Error).message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                else -> { /* Ready / Idle */ }
            }

            // ===== 世界状态面板 =====
            worldState?.let { state ->
                StatePanel(state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 当前事件 =====
            if (activeEvents.isNotEmpty()) {
                Text(
                    "📜 待处理事务",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                activeEvents.forEach { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "[${event.title}] ${event.description}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ===== 推演结果叙事 =====
            narrative?.let { narration ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = narration,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ===== 诏书输入 =====
            Text(
                "✍️ 写诏书",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = edictText,
                onValueChange = { edictText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        "例：朕决意整顿京营，清查吃空饷者……",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ===== 发布按钮 =====
            Button(
                onClick = {
                    if (edictText.isNotBlank()) {
                        viewModel.executeTurn(edictText)
                        edictText = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = edictText.isNotBlank() &&
                        engineState !is EngineUiState.Processing
            ) {
                Text(
                    "📨 下旨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ===== Token 统计 =====
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "已消耗: ${String.format("%.4f", viewModel.tokenTracker.totalCost.totalCost)} 元 | " +
                        "调用: ${viewModel.tokenTracker.totalCost.callCount} 次",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // ===== 错误提示 =====
            error?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 世界状态数值面板
 */
@Composable
private fun StatePanel(state: WorldState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 时间和皇帝
            Text(
                "正德${state.year - 1505 + 1}年 · ${state.month}月",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 国势四维
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("国库", state.treasury, 9999)
                StatItem("军力", state.military, 999)
                StatItem("民心", avgPopularSupport(state), 100)
                StatItem("朝纲", state.courtStability, 100)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 皇帝属性
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("威望", state.prestige, 100)
                StatItem("精力", state.energy, 100)
                StatItem("玩心", state.playfulness, 100)
                StatItem("智慧", state.politicalWisdom, 100)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, max: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                value < max * 0.2 -> MaterialTheme.colorScheme.error
                value < max * 0.4 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun avgPopularSupport(state: WorldState): Int {
    if (state.provinces.isEmpty()) return 50
    return state.provinces.values.map { it.popularSupport }.average().toInt()
}
