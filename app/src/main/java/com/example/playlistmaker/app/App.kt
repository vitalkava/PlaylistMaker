package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase

class App : Application() {

    private lateinit var switchThemeUseCase: SwitchThemeUseCase
    private lateinit var getCurrentThemeUseCase: GetCurrentThemeUseCase

    override fun onCreate() {
        super.onCreate()
        switchThemeUseCase = Creator.provideSwitchThemeUseCase(this)
        getCurrentThemeUseCase = Creator.provideGetCurrentThemeUseCase(this)
        val isDarkTheme = getCurrentThemeUseCase.execute()
        switchTheme(isDarkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        switchThemeUseCase.execute(darkThemeEnabled)
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}