package com.zhengde.chronicles

import android.app.Application
import com.zhengde.chronicles.data.repository.GameRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZhengdeApp : Application() {

    @Inject
    lateinit var gameRepository: GameRepository

    override fun onCreate() {
        super.onCreate()
        gameRepository.init(this)
    }
}
