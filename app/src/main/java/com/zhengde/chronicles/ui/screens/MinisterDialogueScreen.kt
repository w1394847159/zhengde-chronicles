package com.zhengde.chronicles.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhengde.chronicles.game.world.Minister

/**
 * 大臣奏对页面 — 与大臣对话
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinisterDialogueScreen(
    minister: Minister,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    dialogueHistory: List<DialogueMessage>,
    isProcessing: Boolean
) {
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(minister.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(minister.title, fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← 回宫", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 大臣信息头
            MinisterHeader(minister)

            // 对话历史
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dialogueHistory.isEmpty()) {
                    // 开场白
                    DialogueBubble(
                        message = DialogueMessage(
                            text = minister.description + "\n\n" + "陛下今日召臣前来，不知有何事垂询？",
                            isMinister = true,
                            ministerName = minister.name
                        )
                    )
                } else {
                    dialogueHistory.forEach { msg ->
                        DialogueBubble(message = msg)
                    }
                }

                if (isProcessing) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${minister.name}正在思索……",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 底部输入
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        placeholder = {
                            Text("问策于${minister.name}……", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isProcessing
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("奏对")
                    }
                }
            }
        }
    }
}

/**
 * 大臣信息头部
 */
@Composable
private fun MinisterHeader(minister: Minister) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = factionColor(minister.faction).copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 派系标识
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(factionColor(minister.faction).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = minister.faction.take(1),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = factionColor(minister.faction)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = minister.personality,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = minister.stance,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatLabel("政治", minister.politics)
                    StatLabel("军事", minister.military)
                    StatLabel("智力", minister.intelligence)
                }
            }
        }
    }
}

@Composable
private fun StatLabel(label: String, value: Int) {
    Text(
        text = "$label $value",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 对话气泡
 */
@Composable
private fun DialogueBubble(message: DialogueMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMinister) Alignment.Start else Alignment.End
    ) {
        // 说话人名称
        Text(
            text = if (message.isMinister) message.ministerName else "朕",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        // 气泡
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMinister)
                    factionColorForName(message.ministerName).copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(
                topStart = if (message.isMinister) 4.dp else 16.dp,
                topEnd = if (message.isMinister) 16.dp else 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** 对话消息 */
data class DialogueMessage(
    val text: String,
    val isMinister: Boolean = true,
    val ministerName: String = ""
)

/** 根据大臣名字取颜色 */
private fun factionColorForName(name: String): Color = when (name) {
    "刘瑾", "张永", "马永成", "谷大用", "魏彬" -> Color(0xFFD4380D)
    "江彬", "许泰", "神周" -> Color(0xFF7C4E2E)
    "王守仁" -> Color(0xFF3A7D44)
    "朱宸濠" -> Color(0xFF6B3FA0)
    else -> Color(0xFFD4A84B)
}
