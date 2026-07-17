package com.zhengde.chronicles.di

import android.content.Context
import com.zhengde.chronicles.agent.LlmClient
import com.zhengde.chronicles.agent.PromptBuilder
import com.zhengde.chronicles.agent.TokenTracker
import com.zhengde.chronicles.data.db.AppDatabase
import com.zhengde.chronicles.data.db.GameDao
import com.zhengde.chronicles.data.repository.GameRepository
import com.zhengde.chronicles.game.engine.*
import com.zhengde.chronicles.game.features.*
import com.zhengde.chronicles.game.features.baofang.BaoFangFeature
import com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature
import com.zhengde.chronicles.game.features.expedition.ExpeditionFeature
import com.zhengde.chronicles.game.features.incognito.IncognitoFeature
import com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature
import com.zhengde.chronicles.game.memory.MemorySystem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideGameRepository(): GameRepository = GameRepository()

    @Provides
    @Singleton
    fun provideStateManager(): StateManager = StateManager()

    @Provides
    @Singleton
    fun provideEffectSystem(): EffectSystem = EffectSystem()

    @Provides
    @Singleton
    fun provideEventSystem(): EventSystem = EventSystem()

    @Provides
    @Singleton
    fun provideNarrativeSystem(): NarrativeSystem = NarrativeSystem()

    @Provides
    @Singleton
    fun provideMemorySystem(): MemorySystem = MemorySystem()

    @Provides
    @Singleton
    fun provideLlmClient(): LlmClient = LlmClient()

    @Provides
    @Singleton
    fun provideTokenTracker(): TokenTracker = TokenTracker()

    @Provides
    @Singleton
    fun providePromptBuilder(
        memorySystem: MemorySystem,
        stateManager: StateManager
    ): PromptBuilder = PromptBuilder(memorySystem, stateManager)

    // ===== 特色系统（手动创建，避免 Hilt KSP 类型链解析问题） =====

    @Provides
    @Singleton
    fun provideBaoFangFeature(): com.zhengde.chronicles.game.features.baofang.BaoFangFeature {
        return com.zhengde.chronicles.game.features.baofang.BaoFangFeature()
    }

    @Provides
    @Singleton
    fun provideExpeditionFeature(): com.zhengde.chronicles.game.features.expedition.ExpeditionFeature {
        return com.zhengde.chronicles.game.features.expedition.ExpeditionFeature()
    }

    @Provides
    @Singleton
    fun provideIncognitoFeature(): com.zhengde.chronicles.game.features.incognito.IncognitoFeature {
        return com.zhengde.chronicles.game.features.incognito.IncognitoFeature()
    }

    @Provides
    @Singleton
    fun provideEightTigersFeature(): com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature {
        return com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature()
    }

    @Provides
    @Singleton
    fun provideWangYangMingFeature(): com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature {
        return com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature()
    }

    @Provides
    @Singleton
    fun provideFeatureManager(
        baoFang: com.zhengde.chronicles.game.features.baofang.BaoFangFeature,
        expedition: com.zhengde.chronicles.game.features.expedition.ExpeditionFeature,
        incognito: com.zhengde.chronicles.game.features.incognito.IncognitoFeature,
        eightTigers: com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature,
        wangYangMing: com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature
    ): com.zhengde.chronicles.game.features.FeatureManager {
        return com.zhengde.chronicles.game.features.FeatureManager(
            baoFang, expedition, incognito, eightTigers, wangYangMing
        )
    }

    @Provides
    @Singleton
    fun provideWorldEngine(
        stateManager: StateManager,
        effectSystem: EffectSystem,
        eventSystem: EventSystem,
        memorySystem: MemorySystem,
        narrativeSystem: NarrativeSystem,
        llmClient: LlmClient,
        promptBuilder: PromptBuilder,
        tokenTracker: TokenTracker
    ): WorldEngine = WorldEngine(
        stateManager, effectSystem, eventSystem,
        memorySystem, narrativeSystem,
        llmClient, promptBuilder, tokenTracker
    )
}
