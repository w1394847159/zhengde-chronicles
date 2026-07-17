package com.zhengde.chronicles.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 暗金/朱红/墨黑 — 明清皇家色调
private val ZhengdeColorScheme = darkColorScheme(
    primary = Color(0xFFD4A84B),         // 暗金
    onPrimary = Color(0xFF1A0A00),
    primaryContainer = Color(0xFF3A2A10),
    onPrimaryContainer = Color(0xFFF0D68A),
    secondary = Color(0xFF8B1A1A),        // 朱红
    onSecondary = Color(0xFFFFF0E0),
    secondaryContainer = Color(0xFF4A0E0E),
    onSecondaryContainer = Color(0xFFFFC0A0),
    tertiary = Color(0xFF5A3A1A),         // 赭石
    onTertiary = Color(0xFFFFE8C0),
    background = Color(0xFF1A0A00),       // 墨黑
    onBackground = Color(0xFFF0E8D8),
    surface = Color(0xFF2A1A0A),          // 深褐
    onSurface = Color(0xFFF0E8D8),
    surfaceVariant = Color(0xFF3A2A1A),
    onSurfaceVariant = Color(0xFFD0C0A8),
    outline = Color(0xFF6A5A4A),
)

@Composable
fun ZhengdeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZhengdeColorScheme,
        content = content
    )
}
