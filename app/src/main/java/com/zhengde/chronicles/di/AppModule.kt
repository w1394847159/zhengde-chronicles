package com.zhengde.chronicles.di

import android.content.Context
import com.zhengde.chronicles.agent.LlmClient
import com.zhengde.chronicles.agent.PromptBuilder
import com.zhengde.chronicles.agent.TokenTracker
import com.zhengde.chronicles.data.db.AppDatabase
import com.zhengde.chronicles.data.db.GameDao
import com.zhengde.chronicles.data.repository.GameRepository
import com.zhengde.chronicles.game.engine.*
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
    fun provideStateManager(ministerManager: com.zhengde.chronicles.game.engine.MinisterManager): StateManager = StateManager(ministerManager)

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
