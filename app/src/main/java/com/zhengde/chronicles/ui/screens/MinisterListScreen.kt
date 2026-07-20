package com.zhengde.chronicles.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.zhengde.chronicles.data.repository.MinisterData
import com.zhengde.chronicles.game.world.Minister

/**
 * 大臣列表页面 — 展示所有朝堂官员
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinisterListScreen(
    onMinisterClick: (Minister) -> Unit,
    onBack: () -> Unit
) {
    val ministers = remember { MinisterData.getAllMinisters() }
    var selectedFaction by remember { mutableStateOf<String?>(null) }

    val factions = listOf("全部", "内阁", "八虎", "边军", "心学", "宗室", "宫廷")
    val filteredMinisters = if (selectedFaction == null || selectedFaction == "全部") {
        ministers
    } else {
        ministers.filter { it.faction == selectedFaction }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("朝堂 · 百官", fontWeight = FontWeight.Bold)
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
            // 派系筛选
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (faction in factions) {
                    val isSelected = if (faction == "全部") selectedFaction == null
                    else selectedFaction == faction

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFaction = if (faction == "全部") null else faction
                        },
                        label = { Text(faction, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // 大臣数量统计
            Text(
                "共 ${filteredMinisters.size} 位官员",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 大臣列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMinisters, key = { it.id }) { minister ->
                    MinisterCard(
                        minister = minister,
                        onClick = { onMinisterClick(minister) }
                    )
                }
            }
        }
    }
}

/**
 * 大臣卡片 — 仿奏折样式
 */
@Composable
private fun MinisterCard(
    minister: Minister,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 官职缩写（左侧装饰）
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(factionColor(minister.faction).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = minister.title.take(2),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = factionColor(minister.faction),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 姓名+官职
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = minister.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = minister.title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 派系标签
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(factionColor(minister.faction).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = minister.faction,
                    fontSize = 11.sp,
                    color = factionColor(minister.faction),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** 派系主题色 */
fun factionColor(faction: String): Color = when (faction) {
    "八虎" -> Color(0xFFD4380D)     // 赤红
    "内阁" -> Color(0xFFD4A84B)     // 暗金
    "边军" -> Color(0xFF7C4E2E)     // 褐色
    "心学" -> Color(0xFF3A7D44)     // 青绿
    "宗室" -> Color(0xFF6B3FA0)     // 紫色
    "宫廷" -> Color(0xFFB8633C)     // 橘褐
    else -> Color(0xFF888888)
}
