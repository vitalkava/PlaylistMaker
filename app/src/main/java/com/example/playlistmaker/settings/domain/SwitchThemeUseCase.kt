package com.example.playlistmaker.settings.domain

import androidx.appcompat.app.AppCompatDelegate

class SwitchThemeUseCase(private val repository: ThemeRepository) {

    fun execute(isDarkTheme: Boolean) {
        repository.setTheme(isDarkTheme)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}