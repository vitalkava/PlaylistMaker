package com.example.playlistmaker.domain.use_case

import com.example.playlistmaker.domain.api.ThemeRepository

class SwitchThemeUseCase(private val repository: ThemeRepository) {

    fun execute(isDarkTheme: Boolean) {
        repository.setTheme(isDarkTheme)
    }

}