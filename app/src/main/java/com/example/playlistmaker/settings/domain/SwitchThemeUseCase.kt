package com.example.playlistmaker.settings.domain

class SwitchThemeUseCase(private val repository: ThemeRepository) {

    fun execute(isDarkTheme: Boolean) {
        repository.setTheme(isDarkTheme)
    }

}