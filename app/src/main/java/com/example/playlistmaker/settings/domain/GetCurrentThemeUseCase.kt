package com.example.playlistmaker.settings.domain

class GetCurrentThemeUseCase(private val repository: ThemeRepository) {

    fun execute():Boolean {
        return repository.isDarkTheme()
    }
}