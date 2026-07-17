package com.zhengde.chronicles

import android.app.Application
import com.zhengde.chronicles.data.repository.GameRepository
import com.zhengde.chronicles.game.engine.WorldEngine
import com.zhengde.chronicles.game.features.FeatureManager
import com.zhengde.chronicles.game.features.baofang.BaoFangFeature
import com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature
import com.zhengde.chronicles.game.features.expedition.ExpeditionFeature
import com.zhengde.chronicles.game.features.incognito.IncognitoFeature
import com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZhengdeApp : Application() {

    @Inject
    lateinit var gameRepository: GameRepository

    @Inject
    lateinit var worldEngine: WorldEngine

    override fun onCreate() {
        super.onCreate()
        gameRepository.init(this)

        // 手动创建 FeatureManager（绕过 Hilt KSP 依赖链）
        val featureManager = FeatureManager(
            BaoFangFeature(),
            ExpeditionFeature(),
            IncognitoFeature(),
            EightTigersFeature(),
            WangYangMingFeature()
        )
        worldEngine.featureManager = featureManager
    }
}
