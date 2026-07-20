package com.zhengde.chronicles.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.zhengde.chronicles.game.world.Minister
import com.zhengde.chronicles.ui.screens.*

/**
 * 页面路由状态
 */
sealed class Screen {
    data object MainGame : Screen()
    data object MinisterList : Screen()
    data class MinisterDialogue(val minister: Minister) : Screen()
    data object BaoFang : Screen()
    data object Expedition : Screen()
    data object Settings : Screen()
}

/**
 * 简易导航状态管理
 */
class NavigationState {
    var currentScreen: Screen by mutableStateOf(Screen.MainGame)
        private set

    private val backStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun goBack(): Boolean {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
            return true
        }
        return false
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}
