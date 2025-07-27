package com.example.playlistmaker.app

import android.app.Application
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase

class App : Application() {

    private lateinit var switchThemeUseCase: SwitchThemeUseCase

    override fun onCreate() {
        super.onCreate()
        val themeRepository = Creator.provideThemeRepository(this)
        switchThemeUseCase = Creator.provideSwitchThemeUseCase(this)
        val isDarkTheme = themeRepository.isDarkTheme()
        switchTheme(isDarkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        switchThemeUseCase.execute(darkThemeEnabled)
    }
}